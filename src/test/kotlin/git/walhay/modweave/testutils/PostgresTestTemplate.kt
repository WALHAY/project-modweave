package git.walhay.modweave.testutils

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer

@DataJpaTest
@Testcontainers
class PostgresTestTemplate {
  companion object {
    @Container
    @JvmStatic
    @ServiceConnection
    val pgsqlContainer =
        PostgreSQLContainer("postgres:latest").apply {
          withInitScript("init.sql")
          withReuse(true)
        }
  }
}
