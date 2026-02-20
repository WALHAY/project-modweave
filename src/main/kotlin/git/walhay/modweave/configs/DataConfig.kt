package git.walhay.modweave.configs

import io.minio.MinioClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.DriverManagerDataSource

@Configuration
class DataConfig
@Autowired
constructor(
    @Value("\${minio.endpoint}") private val endpoint: String,
    @Value("\${minio.credentials.username}") private val accessKey: String,
    @Value("\${minio.credentials.password}") private val secretKey: String,
) {
  @Bean
  fun dataSource() =
      DriverManagerDataSource().apply {
        username = "postgres"
        password = "postgres"
        schema = "modweave"
        catalog = "production"
        url = "jdbc:postgresql://localhost:5432/"
        setDriverClassName("org.postgresql.Driver")
      }

  @Bean
  fun minioClient(): MinioClient =
      MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build()
}
