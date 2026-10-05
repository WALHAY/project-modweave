package git.walhay.modweave.config

import java.time.Duration
import mu.KLogger
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory

@Configuration
@EnableCaching
@ConditionalOnProperty(name = ["spring.cache.type"], havingValue = "redis", matchIfMissing = true)
class CacheConfiguration(
    @param:Value("\${spring.cache.redis.time-to-live:3600000}") private val ttlMillis: Long,
) {
  private val logger: KLogger = KotlinLogging.logger {}

  @Bean
  fun cacheManager(redisConnectionFactory: RedisConnectionFactory): RedisCacheManager {
    logger.debug {
      "Initializing RedisCacheManager with connection factory: $redisConnectionFactory"
    }
    val configuration =
        RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMillis(ttlMillis))
            .disableCachingNullValues()

    return RedisCacheManager.builder(redisConnectionFactory)
        .cacheDefaults(configuration)
        .transactionAware()
        .build()
  }
}
