package git.walhay.modweave.integration

import git.walhay.modweave.api.game.GameId
import git.walhay.modweave.api.game.IGameService
import git.walhay.modweave.api.game.command.GameCreateCommand
import git.walhay.modweave.api.mod.IModService
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.mod.command.ModCreateCommand
import git.walhay.modweave.api.storage.*
import git.walhay.modweave.api.user.UserId
import git.walhay.modweave.testutils.StorageTestInfrastructure
import io.minio.*
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.multipart.MultipartFile

@SpringBootTest(properties = [
  "spring.cache.type=none", "storage.cleanup.enabled=false", "storage.cleanup.upload-grace-ms=0",
  "security.jwt.secret=ZmFrZS1zZWNyZXQtc2VjdXJlLWtleS1mb3ItanVzdC1kZXZlbG9wbWVudA=="
])
@Import(StorageTransactionsTest.FaultConfig::class)
class StorageTransactionsTest {
  companion object {
    @JvmStatic @DynamicPropertySource
    fun properties(registry: DynamicPropertyRegistry) {
      registry.add("spring.datasource.url") { StorageTestInfrastructure.postgres.jdbcUrl }
      registry.add("spring.datasource.username") { StorageTestInfrastructure.postgres.username }
      registry.add("spring.datasource.password") { StorageTestInfrastructure.postgres.password }
      registry.add("storage.s3.endpoint") { StorageTestInfrastructure.endpoint }
    }
  }

  class FailingStorage(private val delegate: S3ObjectStorage) : ObjectStorageClient {
    var putCount = 0
    var failBeforePut = 0
    var failAfterPut = 0
    var failDelete = false
    var afterPut: (() -> Unit)? = null
    override fun upload(bucket: StorageBucket, filename: String, file: MultipartFile): String {
      putCount++
      if (putCount == failBeforePut) throw IOException("S3 unavailable before PUT")
      val result = delegate.upload(bucket, filename, file)
      afterPut?.invoke()
      if (putCount == failAfterPut) throw IOException("PUT succeeded but response was lost")
      return result
    }
    override fun remove(bucket: StorageBucket, filename: String) {
      if (failDelete) throw IOException("S3 unavailable during DELETE")
      delegate.remove(bucket, filename)
    }
    override fun downloadVersionFile(filename: String): InputStream = delegate.downloadVersionFile(filename)
  }

  @TestConfiguration(proxyBeanMethods = false)
  class FaultConfig {
    @Bean @Primary fun failingStorage(storage: S3ObjectStorage) = FailingStorage(storage)
  }

  @Autowired lateinit var mods: IModService
  @Autowired lateinit var games: IGameService
  @Autowired lateinit var storage: ISimpleStorageService
  @Autowired lateinit var raw: S3ObjectStorage
  @Autowired lateinit var failing: FailingStorage
  @Autowired lateinit var operations: StorageOperationRepository
  @Autowired lateinit var cleanup: StorageCleanupProcessor
  @Autowired lateinit var cleanupProperties: StorageCleanupProperties
  @Autowired lateinit var transactionManager: PlatformTransactionManager
  @Autowired lateinit var jdbc: JdbcTemplate
  @Autowired lateinit var client: MinioClient

  private val owner = UserId("owner")
  private fun file(name: String = "mod.zip") = MockMultipartFile("files", name, "application/octet-stream", byteArrayOf(1,2,3))
  private fun keys(bucket: String): Set<String> = client.listObjects(ListObjectsArgs.builder().bucket(bucket).recursive(true).build()).map { it.get().objectName() }.toSet()
  private fun count(table: String): Int = jdbc.queryForObject("select count(*) from modweave.$table", Int::class.java)!!
  private fun upload(name: String = "example", files: List<MultipartFile> = listOf(file())) = mods.uploadMod(owner,
      ModCreateCommand(ModId(name), name, null, file("logo.png"), versionName = "1.0", files = files, gameId = GameId("fixture")))

  @BeforeEach fun prepare() {
    failing.putCount = 0; failing.failBeforePut = 0; failing.failAfterPut = 0; failing.failDelete = false; failing.afterPut = null
    for (bucket in listOf("mods", "images")) for (key in keys(bucket)) client.removeObject(RemoveObjectArgs.builder().bucket(bucket).`object`(key).build())
    jdbc.execute("truncate modweave.users, modweave.games, modweave.storage_cleanup_tasks cascade")
    jdbc.update("insert into modweave.users (username, name, email, password) values ('owner', 'Owner', 'owner@example.com', 'unused')")
    jdbc.update("insert into modweave.games (id, name, image_path) values ('fixture', 'Fixture', 'fixture.png')")
    jdbc.execute("""
      create or replace function modweave.fail_test_commit() returns trigger language plpgsql as '
      begin
        if TG_OP = ''INSERT'' and NEW.name = ''fail-commit'' then raise exception ''injected commit failure''; end if;
        if TG_OP = ''DELETE'' and OLD.name = ''fail-delete'' then raise exception ''injected delete commit failure''; end if;
        return null;
      end;'
      """.trimIndent())
    jdbc.execute("drop trigger if exists test_commit_failure on modweave.mods")
    jdbc.execute("create constraint trigger test_commit_failure after insert or delete on modweave.mods deferrable initially deferred for each row execute function modweave.fail_test_commit()")
    SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken("owner", null, listOf(SimpleGrantedAuthority("ROLE_ADMIN")))
  }

  @AfterEach fun finish() { SecurityContextHolder.clearContext() }

  @Test fun `successful publication commits database references and objects with no cleanup pending`() {
    upload(files = listOf(file("one.zip"), file("two.zip")))
    assertEquals(1, count("mods")); assertEquals(1, count("mod_versions")); assertEquals(2, count("mod_files"))
    assertEquals(1, keys("images").size); assertEquals(2, keys("mods").size); assertEquals(0, count("storage_cleanup_tasks"))
  }

  @Test fun `checked S3 exception rolls back all database records and cleans earlier uploads`() {
    failing.failBeforePut = 3 // Image and first file succeed; second file fails.
    assertThrows(Exception::class.java) { upload(files = listOf(file("one.zip"), file("two.zip"))) }
    assertEquals(0, count("mods")); assertEquals(0, count("mod_versions")); assertEquals(0, count("mod_files"))
    assertEquals(3, count("storage_cleanup_tasks"))
    cleanup.runBatch()
    assertTrue(keys("images").isEmpty()); assertTrue(keys("mods").isEmpty()); assertEquals(0, count("storage_cleanup_tasks"))
  }

  @Test fun `lost PUT response leaves a durable intent for the object already stored`() {
    failing.failAfterPut = 1
    assertThrows(Exception::class.java) { upload() }
    assertEquals(0, count("mods")); assertEquals(1, keys("images").size); assertEquals(1, count("storage_cleanup_tasks"))
    cleanup.runBatch()
    assertTrue(keys("images").isEmpty()); assertEquals(0, count("storage_cleanup_tasks"))
  }

  @Test fun `database COMMIT failure restores intents for all uploaded objects`() {
    assertThrows(Exception::class.java) { upload("fail-commit") }
    assertEquals(0, count("mods")); assertEquals(0, count("mod_versions")); assertEquals(0, count("mod_files"))
    assertEquals(1, keys("images").size); assertEquals(1, keys("mods").size); assertEquals(2, count("storage_cleanup_tasks"))
    cleanup.runBatch()
    assertTrue(keys("images").isEmpty()); assertTrue(keys("mods").isEmpty())
  }

  @Test fun `database failure before journaling prevents the S3 write`() {
    jdbc.execute("alter table modweave.storage_cleanup_tasks add constraint test_reject_intents check (false) not valid")
    try {
      assertThrows(Exception::class.java) { upload() }
      assertEquals(0, failing.putCount); assertTrue(keys("images").isEmpty()); assertEquals(0, count("mods"))
    } finally { jdbc.execute("alter table modweave.storage_cleanup_tasks drop constraint test_reject_intents") }
  }

  @Test fun `failed cleanup survives processor restart and retries after S3 recovers`() {
    val mod = upload()
    mods.deleteMod(owner, mod.id)
    assertEquals(0, count("mods")); assertEquals(2, count("storage_cleanup_tasks"))
    failing.failDelete = true
    cleanup.runBatch()
    assertEquals(2, count("storage_cleanup_tasks")); assertEquals(1, keys("mods").size)
    assertEquals(2, jdbc.queryForObject("select sum(attempts) from modweave.storage_cleanup_tasks", Int::class.java))
    failing.failDelete = false
    jdbc.update("update modweave.storage_cleanup_tasks set next_attempt_at = current_timestamp")
    StorageCleanupProcessor(operations, failing, transactionManager, cleanupProperties).runBatch()
    assertEquals(0, count("storage_cleanup_tasks")); assertTrue(keys("images").isEmpty()); assertTrue(keys("mods").isEmpty())
  }

  @Test fun `delete COMMIT failure preserves both database records and S3 objects`() {
    val mod = upload("fail-delete")
    assertThrows(Exception::class.java) { mods.deleteMod(owner, mod.id) }
    cleanup.runBatch()
    assertEquals(1, count("mods")); assertEquals(1, count("mod_files")); assertEquals(0, count("storage_cleanup_tasks"))
    assertEquals(1, keys("images").size); assertEquals(1, keys("mods").size)
  }

  @Test fun `worker preserves an object that is still referenced by committed data`() {
    val mod = upload()
    operations.registerUpload(StorageBucket.IMAGES, mod.imagePath)
    cleanup.runBatch()
    assertTrue(keys("images").contains(mod.imagePath)); assertEquals(0, count("storage_cleanup_tasks"))
  }

  @Test fun `worker skips an active transaction even after upload grace elapsed`() {
    val put = CountDownLatch(1); val release = CountDownLatch(1)
    failing.afterPut = { put.countDown(); check(release.await(15, TimeUnit.SECONDS)) }
    Executors.newSingleThreadExecutor().use { executor ->
      val publication = executor.submit {
        TransactionTemplate(transactionManager).execute {
          storage.uploadImage("concurrent.png", file("logo.png"))
          jdbc.update("insert into modweave.games (id, name, image_path) values ('concurrent', 'Concurrent', 'concurrent.png')")
        }
      }
      try {
        assertTrue(put.await(15, TimeUnit.SECONDS))
        cleanup.runBatch()
        assertTrue(keys("images").contains("concurrent.png")); assertEquals(1, count("storage_cleanup_tasks"))
      } finally { release.countDown() }
      publication.get(15, TimeUnit.SECONDS)
    }
    assertEquals(0, count("storage_cleanup_tasks")); assertTrue(keys("images").contains("concurrent.png"))
  }

  @Test fun `game image obeys the same rollback protocol`() {
    failing.failAfterPut = 1
    assertThrows(Exception::class.java) { games.uploadGame(GameCreateCommand(GameId("new-game"), "New game", null, file("logo.png"))) }
    assertEquals(1, count("games")); assertEquals(1, count("storage_cleanup_tasks"))
    cleanup.runBatch()
    assertTrue(keys("images").isEmpty())
  }
}
