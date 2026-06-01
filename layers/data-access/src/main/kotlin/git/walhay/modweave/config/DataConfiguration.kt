package git.walhay.modweave.config

import io.minio.MinioClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DataConfiguration(
    @param:Value("\${minio.endpoint}") private val endpoint: String,
    @param:Value("\${minio.username}") private val accessKey: String,
    @param:Value("\${minio.password}") private val secretKey: String,
) {
  @Bean
  fun minioClient(): MinioClient =
      MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build()
}
