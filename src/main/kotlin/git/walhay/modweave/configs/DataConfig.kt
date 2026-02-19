package git.walhay.modweave.configs

import io.minio.MinioClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.DriverManagerDataSource

@Configuration
class DataConfig {
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
    fun minioClient(): MinioClient = MinioClient.builder().endpoint("http://localhost:9000")
            .credentials("miniouser", "miniopass").build()
}
