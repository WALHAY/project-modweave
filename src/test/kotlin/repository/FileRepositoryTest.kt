package git.walhay.modweave.repository

import git.walhay.modweave.domain.file.File
import git.walhay.modweave.domain.file.FileId
import git.walhay.modweave.domain.file.FileRepository
import git.walhay.modweave.domain.game.Game
import git.walhay.modweave.domain.game.GameRepository
import git.walhay.modweave.domain.mod.Mod
import git.walhay.modweave.domain.mod.ModId
import git.walhay.modweave.domain.mod.ModRepository
import git.walhay.modweave.domain.user.User
import git.walhay.modweave.domain.user.UserRepository
import git.walhay.modweave.domain.version.Version
import git.walhay.modweave.domain.version.VersionId
import git.walhay.modweave.domain.version.VersionRepository
import git.walhay.modweave.persistence.file.JpaFileRepository
import git.walhay.modweave.persistence.game.JpaGameRepository
import git.walhay.modweave.persistence.mod.JpaModRepository
import git.walhay.modweave.persistence.user.JpaUserRepository
import git.walhay.modweave.persistence.version.JpaVersionRepository
import git.walhay.modweave.testutils.PostgresTestTemplate
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import

@Import(
    JpaFileRepository::class,
    JpaVersionRepository::class,
    JpaModRepository::class,
    JpaGameRepository::class,
    JpaUserRepository::class,
)
class FileRepositoryTest : PostgresTestTemplate() {
  @Autowired lateinit var fileRepository: FileRepository

  @Autowired lateinit var versionRepository: VersionRepository

  @Autowired lateinit var modRepository: ModRepository

  @Autowired lateinit var gameRepository: GameRepository

  @Autowired lateinit var userRepository: UserRepository

  private fun seedVersion(): Version {
    val publisher = userRepository.save(User("publisher", "publisher", "publisher@mail.ru", "pass"))
    val game = gameRepository.save(Game(name = "Game", description = null, imagePath = "img.png"))
    val mod =
        modRepository.save(
            Mod(
                id = ModId("mod-1"),
                name = "Mod",
                description = null,
                imagePath = "img.png",
                publisherId = publisher.username,
                gameId = game.id,
            ),
        )

    return versionRepository.save(Version(name = "1.0.0", changes = null, modId = mod.id))
  }

  private fun seedFile(): File {
    val version = seedVersion()
    return fileRepository.save(
        File(
            filename = "file.zip",
            filePath = "mods/${version.modId.value}/${version.name}/file.zip",
            versionId = VersionId(version.id.value),
        ),
    )
  }

  @Test
  fun `create file`() {
    val created = seedFile()
    assertNotNull(created)
    assertNotEquals(java.util.UUID(0, 0), created.id.value)
  }

  @Test
  fun `read file`() {
    val created = seedFile()

    val byId = fileRepository.findById(FileId(created.id.value))
    assertNotNull(byId)
    assertEquals(created.filePath, byId!!.filePath)

    val byPath = fileRepository.findByFilePath(created.filePath)
    assertNotNull(byPath)
    assertEquals(created.id.value, byPath!!.id.value)

    val byVersion = fileRepository.findAllByVersionId(created.versionId)
    assertTrue(byVersion.any { it.id.value == created.id.value })
  }

  @Test
  fun `update file`() {
    val created = seedFile()

    val updated = fileRepository.save(created.copy(filename = "file-2.zip"))
    assertEquals(created.id.value, updated.id.value)

    val refetched = fileRepository.findById(updated.id)
    assertNotNull(refetched)
    assertEquals("file-2.zip", refetched!!.filename)
  }

  @Test
  fun `delete file`() {
    val created = seedFile()

    fileRepository.deleteById(created.id)

    val after = fileRepository.findById(created.id)
    assertNull(after)
  }

  @Autowired lateinit var entityManager: jakarta.persistence.EntityManager

  @Test
  fun `downloads are incremented in the database`() {
    val file = seedFile()
    entityManager.flush()
    fileRepository.incrementDownloads(file.id)
    fileRepository.incrementDownloads(file.id)
    entityManager.clear()
    assertEquals(2, fileRepository.findById(file.id)!!.downloads)
  }
}
