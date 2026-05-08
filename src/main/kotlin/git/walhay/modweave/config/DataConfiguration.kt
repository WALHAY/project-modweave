package git.walhay.modweave.config

import io.minio.MinioClient
import mu.KLogger
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DataConfiguration
@Autowired
constructor(
    @param:Value($$"${minio.endpoint}") private val endpoint: String,
    @param:Value($$"${minio.username}") private val accessKey: String,
    @param:Value($$"${minio.password}") private val secretKey: String,
) {
  private val logger: KLogger = KotlinLogging.logger {}

  @Bean
  fun minioClient(): MinioClient {
    logger.info { "Configuring MinIO client with endpoint: $endpoint" }
    return MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build().also {
      logger.info { "MinIO client configured successfully" }
    }
  }
}
