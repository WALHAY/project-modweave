package git.walhay.modweave.config

import io.minio.MinioClient
import mu.KLogger
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.DriverManagerDataSource

@Configuration
class DataConfiguration
@Autowired
constructor(
    @param:Value($$"${minio.endpoint}") private val endpoint: String,
    @param:Value($$"${minio.credentials.username}") private val accessKey: String,
    @param:Value($$"${minio.credentials.password}") private val secretKey: String,
) {
  private val logger: KLogger = KotlinLogging.logger {}

  @Bean
  fun dataSource() =
      DriverManagerDataSource().apply {
        logger.info { "Configuring PostgreSQL data source" }
        username = "postgres"
        password = "postgres"
        schema = "modweave"
        catalog = "production"
        url = "jdbc:postgresql://localhost:5432/"
        setDriverClassName("org.postgresql.Driver")
        logger.info { "PostgreSQL data source configured successfully" }
      }

  @Bean
  fun minioClient(): MinioClient {
    logger.info { "Configuring MinIO client with endpoint: $endpoint" }
    return MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build().also {
      logger.info { "MinIO client configured successfully" }
    }
  }
}
