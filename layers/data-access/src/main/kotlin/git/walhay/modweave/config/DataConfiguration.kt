package git.walhay.modweave.config

import io.minio.MinioClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

@Configuration
@EnableScheduling
class DataConfiguration(
    @param:Value("\${storage.s3.endpoint}") private val endpoint: String,
    @param:Value("\${storage.s3.username}") private val accessKey: String,
    @param:Value("\${storage.s3.password}") private val secretKey: String,
) {
  @Bean
  fun minioClient(): MinioClient =
      MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey)
          .httpClient(OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS)
              .readTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS)
              .callTimeout(2, TimeUnit.MINUTES).build()).build()
}
