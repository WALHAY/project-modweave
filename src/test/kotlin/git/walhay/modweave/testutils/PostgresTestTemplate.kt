package git.walhay.modweave.testutils

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.MountableFile

@DataJpaTest
abstract class PostgresTestTemplate {
  companion object {
    private val container by lazy {
      PostgreSQLContainer("postgres:16-alpine").apply {
        withCopyFileToContainer(
            MountableFile.forClasspathResource("init.sql"),
            "/docker-entrypoint-initdb.d/01-init.sql")
        withCopyFileToContainer(
            MountableFile.forClasspathResource("trigger.sql"),
            "/docker-entrypoint-initdb.d/02-trigger.sql")
        withCopyFileToContainer(
            MountableFile.forClasspathResource("storage-cleanup.sql"),
            "/docker-entrypoint-initdb.d/03-storage-cleanup.sql")
        start()
      }
    }

    @JvmStatic
    @DynamicPropertySource
    fun databaseProperties(registry: DynamicPropertyRegistry) {
      val testUrl = System.getenv("MODWEAVE_TEST_DATABASE_URL")
      if (testUrl != null) {
        registry.add("spring.datasource.url") { testUrl }
        registry.add("spring.datasource.username") {
          System.getenv("MODWEAVE_TEST_DATABASE_USER") ?: "postgres"
        }
        registry.add("spring.datasource.password") {
          System.getenv("MODWEAVE_TEST_DATABASE_PASSWORD") ?: ""
        }
      } else {
        registry.add("spring.datasource.url") { container.jdbcUrl }
        registry.add("spring.datasource.username") { container.username }
        registry.add("spring.datasource.password") { container.password }
      }
    }
  }
}
