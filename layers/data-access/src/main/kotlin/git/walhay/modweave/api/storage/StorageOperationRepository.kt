package git.walhay.modweave.api.storage

import java.sql.ResultSet
import java.util.UUID
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionTemplate

internal data class StorageCleanupTask(
    val id: UUID,
    val bucket: StorageBucket,
    val key: String,
    val attempts: Int
)

@Repository
class StorageOperationRepository(
    private val jdbc: JdbcTemplate,
    transactionManager: PlatformTransactionManager,
    private val properties: StorageCleanupProperties,
) {
  private val independentTransaction =
      TransactionTemplate(transactionManager).apply {
        propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
      }

  /** Save the upload intent before writing the object. */
  fun registerUpload(bucket: StorageBucket, key: String): UUID =
      independentTransaction.execute {
        val id = UUID.randomUUID()
        jdbc.update(
            """
            insert into modweave.storage_cleanup_tasks (id, bucket, object_key, next_attempt_at)
            values (?, ?, ?, current_timestamp + (? * interval '1 millisecond'))
            """
                .trimIndent(),
            id,
            bucket.name,
            key,
            properties.uploadGraceMs)
        id
      }!!

  /** Keep the intent locked until the business transaction finishes. */
  fun lockUpload(id: UUID) {
    val locked =
        jdbc.queryForList(
            "select id from modweave.storage_cleanup_tasks where id = ? for update", id)
    check(locked.isNotEmpty()) { "Upload intent has expired; retry the operation" }
  }

  fun completeUpload(id: UUID) {
    jdbc.update("delete from modweave.storage_cleanup_tasks where id = ?", id)
  }

  /** Called in the same transaction that removes the application records. */
  fun enqueueDeletion(bucket: StorageBucket, key: String) {
    jdbc.update(
        """
        insert into modweave.storage_cleanup_tasks (id, bucket, object_key)
        values (?, ?, ?) on conflict (bucket, object_key) do nothing
        """
            .trimIndent(),
        UUID.randomUUID(),
        bucket.name,
        key)
  }

  /** Claim work before calling S3 so the database transaction stays short. */
  internal fun nextTask(): StorageCleanupTask? =
      independentTransaction.execute {
        val task =
            jdbc
                .query(
                    """
                    select id, bucket, object_key, attempts from modweave.storage_cleanup_tasks
                    where (status = 'PENDING' and next_attempt_at <= current_timestamp)
                       or (status = 'PROCESSING' and claimed_until <= current_timestamp)
                    order by next_attempt_at, created_at, id limit 1 for update skip locked
                    """
                        .trimIndent(),
                    { rs: ResultSet, _: Int ->
                      StorageCleanupTask(
                          rs.getObject("id", UUID::class.java),
                          StorageBucket.valueOf(rs.getString("bucket")),
                          rs.getString("object_key"),
                          rs.getInt("attempts"))
                    })
                .firstOrNull()
        task?.also {
          jdbc.update(
              """
              update modweave.storage_cleanup_tasks
              set status = 'PROCESSING',
                  claimed_until = current_timestamp + (? * interval '1 millisecond')
              where id = ?
              """
                  .trimIndent(),
              properties.claimTimeoutMs,
              it.id)
        }
        task
      }

  fun isReferenced(bucket: StorageBucket, key: String): Boolean =
      when (bucket) {
        StorageBucket.MODS ->
            jdbc.queryForObject(
                "select exists(select 1 from modweave.mod_files where file_path = ?)",
                Boolean::class.java,
                key)!!
        StorageBucket.IMAGES ->
            jdbc.queryForObject(
                """
                select exists(select 1 from modweave.games where image_path = ?
                              union all select 1 from modweave.mods where image_path = ?)
                """
                    .trimIndent(),
                Boolean::class.java,
                key,
                key)!!
      }

  internal fun retryLater(task: StorageCleanupTask, error: Exception) {
    val multiplier = 1L shl task.attempts.coerceIn(0, 10)
    val delay =
        properties.retryDelayMs.coerceAtMost(properties.maxRetryDelayMs / multiplier) * multiplier
    jdbc.update(
        """
        update modweave.storage_cleanup_tasks
        set status = 'PENDING', claimed_until = null, attempts = attempts + 1, last_error = ?,
            next_attempt_at = current_timestamp + (? * interval '1 millisecond')
        where id = ?
        """
            .trimIndent(),
        "${error.javaClass.simpleName}: ${error.message}".take(1000),
        delay,
        task.id)
  }
}
