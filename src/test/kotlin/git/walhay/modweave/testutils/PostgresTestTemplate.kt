package git.walhay.modweave.testutils

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@DataJpaTest
@Testcontainers
class PostgresTestTemplate {

    companion object {
        @Container
        val pgsqlContainer = PostgreSQLContainer<Nothing>(DockerImageName.parse("postgres:latest")).apply {
            withDatabaseName("modweave_test")
            withUsername("test")
            withPassword("test")
            withInitScript("init.sql")
            withReuse(true)
        }
    }

}