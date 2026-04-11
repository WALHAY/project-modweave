package git.walhay.modweave.config

import mu.KLogger
import mu.KotlinLogging
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import java.time.Duration

@Configuration
@EnableCaching
class CacheConfiguration(private val logger: KLogger = KotlinLogging.logger {}) {

  @Bean
  fun cacheManager(redisConnectionFactory: RedisConnectionFactory): RedisCacheManager {
    logger.debug {
      "Initializing RedisCacheManager with connection factory: $redisConnectionFactory"
    }
    val configuration =
        RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(60))
            .disableCachingNullValues()

    return RedisCacheManager.builder(redisConnectionFactory).cacheDefaults(configuration).build()
  }
}
