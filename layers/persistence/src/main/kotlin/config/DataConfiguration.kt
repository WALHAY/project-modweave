package git.walhay.modweave.persistence.config

import java.net.URI
import java.time.Duration
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client

@Configuration(proxyBeanMethods = false)
@EnableScheduling
class DataConfiguration {
  @Bean
  fun s3Client(
      @Value("\${storage.s3.endpoint}") endpoint: URI,
      @Value("\${storage.s3.username}") accessKey: String,
      @Value("\${storage.s3.password}") secretKey: String,
      @Value("\${storage.s3.region}") region: String,
  ): S3Client =
      S3Client.builder()
          .endpointOverride(endpoint)
          .region(Region.of(region))
          .credentialsProvider(
              StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
          .forcePathStyle(true)
          // SeaweedFS 4.48 expects a regular PUT body, without optional checksum trailers.
          .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
          .serviceConfiguration { it.chunkedEncodingEnabled(false) }
          .httpClientBuilder(
              UrlConnectionHttpClient.builder()
                  .connectionTimeout(Duration.ofSeconds(5))
                  .socketTimeout(Duration.ofSeconds(30)))
          .overrideConfiguration { it.apiCallTimeout(Duration.ofMinutes(2)) }
          .build()
}
