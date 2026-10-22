package git.walhay.modweave.config

import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
@EnableCaching
class CacheConfiguration {
  @Bean
  fun transactionalRedisCache(): RedisCacheManagerBuilderCustomizer =
      RedisCacheManagerBuilderCustomizer {
        it.transactionAware()
      }
}
