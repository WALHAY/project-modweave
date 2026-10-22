package git.walhay.modweave.regression

import git.walhay.modweave.config.CacheConfiguration
import java.time.Duration
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.cache.transaction.TransactionAwareCacheDecorator
import org.springframework.data.redis.cache.RedisCache
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory

class CacheConfigurationTest {
  @Test
  fun `Boot configures transactional Redis cache from application properties`() {
    ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(CacheAutoConfiguration::class.java))
        .withUserConfiguration(CacheConfiguration::class.java)
        .withBean(RedisConnectionFactory::class.java, { mock(RedisConnectionFactory::class.java) })
        .withPropertyValues(
            "spring.cache.type=redis",
            "spring.cache.cache-names=versions",
            "spring.cache.redis.time-to-live=1h",
            "spring.cache.redis.key-prefix=modweave:domain-v2:",
            "spring.cache.redis.cache-null-values=false")
        .run { context ->
          assertNull(context.startupFailure)
          val manager = context.getBean(RedisCacheManager::class.java)
          assertTrue(manager.isTransactionAware)
          val cache = manager.getCache("versions") as TransactionAwareCacheDecorator
          val config = (cache.targetCache as RedisCache).cacheConfiguration
          assertEquals(Duration.ofHours(1), config.ttlFunction.getTimeToLive("key", "value"))
          assertEquals("modweave:domain-v2:versions::", config.getKeyPrefixFor("versions"))
          assertFalse(config.allowCacheNullValues)
        }
  }
}
