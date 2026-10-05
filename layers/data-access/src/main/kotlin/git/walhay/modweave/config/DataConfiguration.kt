package git.walhay.modweave.config

import io.minio.MinioClient
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
class DataConfiguration(
    @param:Value("\${storage.s3.endpoint}") private val endpoint: String,
    @param:Value("\${storage.s3.username}") private val accessKey: String,
    @param:Value("\${storage.s3.password}") private val secretKey: String,
    @param:Value("\${storage.s3.region}") private val region: String,
) {
  @Bean
  fun minioClient(): MinioClient =
      MinioClient.builder()
          .endpoint(endpoint)
          .credentials(accessKey, secretKey)
          .region(region)
          .httpClient(
              OkHttpClient.Builder()
                  .connectTimeout(5, TimeUnit.SECONDS)
                  .readTimeout(30, TimeUnit.SECONDS)
                  .writeTimeout(30, TimeUnit.SECONDS)
                  .callTimeout(2, TimeUnit.MINUTES)
                  .build())
          .build()
}
