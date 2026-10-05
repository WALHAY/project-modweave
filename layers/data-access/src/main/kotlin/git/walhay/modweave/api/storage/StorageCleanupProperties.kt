package git.walhay.modweave.api.storage

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("storage.cleanup")
data class StorageCleanupProperties(
    val enabled: Boolean = true,
    val batchSize: Int = 20,
    val uploadGraceMs: Long = 60_000,
    val retryDelayMs: Long = 60_000,
    val maxRetryDelayMs: Long = 3_600_000,
    val claimTimeoutMs: Long = 300_000,
) {
  init {
    require(batchSize > 0)
    require(uploadGraceMs >= 0)
    require(retryDelayMs > 0 && maxRetryDelayMs >= retryDelayMs)
    require(claimTimeoutMs > 0)
  }
}
