package git.walhay.modweave.api.storage

import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionTemplate

@Component
class StorageCleanupProcessor(
    private val operations: StorageOperationRepository,
    private val storage: ObjectStorageClient,
    transactionManager: PlatformTransactionManager,
    private val properties: StorageCleanupProperties,
) {
  private val logger = KotlinLogging.logger {}
  private val transaction =
      TransactionTemplate(transactionManager).apply {
        propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
      }

  @Scheduled(fixedDelayString = "\${storage.cleanup.poll-interval-ms:1000}")
  fun scheduledCleanup() {
    if (properties.enabled) runBatch()
  }

  fun runBatch() {
    repeat(properties.batchSize) {
      val found =
          try {
            transaction.execute {
              val task = operations.nextTask() ?: return@execute false
              // Also protects an upload if a previous COMMIT result was uncertain.
              if (!operations.isReferenced(task.bucket, task.key)) {
                try {
                  storage.remove(task.bucket, task.key)
                } catch (e: Exception) {
                  operations.retryLater(task, e)
                  logger.warn(e) { "Storage cleanup ${task.id} will be retried" }
                  return@execute true
                }
              }
              operations.completeUpload(task.id)
              true
            }
          } catch (e: Exception) {
            logger.warn(e) { "Storage cleanup database unavailable; tasks remain pending" }
            return
          }
      if (!found) return
    }
  }
}
