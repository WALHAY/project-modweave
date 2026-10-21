package git.walhay.modweave.persistence.storage

import git.walhay.modweave.domain.storage.ImageFormat
import git.walhay.modweave.domain.storage.ImageNotFoundException
import java.io.InputStream
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.http.ContentStreamProvider
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.S3Exception

@Service
class S3ObjectStorage(
    private val client: S3Client,
    @param:Value("\${storage.s3.buckets.mods}") private val modsBucket: String,
    @param:Value("\${storage.s3.buckets.images}") private val imagesBucket: String,
) : ObjectStorageClient {
  private val logger = KotlinLogging.logger {}

  @EventListener(ApplicationReadyEvent::class)
  fun initBuckets() {
    for (bucket in setOf(modsBucket, imagesBucket)) {
      try {
        client.headBucket { it.bucket(bucket) }
      } catch (error: S3Exception) {
        if (error.statusCode() != 404) throw error
        client.createBucket { it.bucket(bucket) }
      }
      try {
        client.deleteBucketPolicy { it.bucket(bucket) }
      } catch (error: S3Exception) {
        if (error.awsErrorDetails()?.errorCode() != "NoSuchBucketPolicy") throw error
      }
    }
  }

  override fun upload(bucket: StorageBucket, filename: String, file: MultipartFile): String {
    logger.info {
      "Uploading file=${file.originalFilename} into bucket=$bucket with filename=$filename"
    }
    val contentType =
        if (bucket == StorageBucket.IMAGES) {
          file.inputStream.use { ImageFormat.inspect(it).contentType }
        } else file.contentType ?: "application/octet-stream"
    var input: InputStream? = null
    val streams =
        ContentStreamProvider.fromInputStreamSupplier { file.inputStream.also { input = it } }
    AutoCloseable { input?.close() }
        .use {
          client.putObject(
              { it.bucket(bucketName(bucket)).key(filename).contentType(contentType) },
              RequestBody.fromContentProvider(streams, file.size, contentType))
        }
    return filename
  }

  override fun remove(bucket: StorageBucket, filename: String) {
    client.deleteObject { it.bucket(bucketName(bucket)).key(filename) }
  }

  override fun downloadImage(filename: String): InputStream =
      try {
        client.getObject { it.bucket(imagesBucket).key(filename) }
      } catch (_: NoSuchKeyException) {
        throw ImageNotFoundException()
      }

  override fun downloadVersionFile(filename: String): InputStream =
      client.getObject { it.bucket(modsBucket).key(filename) }

  private fun bucketName(bucket: StorageBucket): String =
      when (bucket) {
        StorageBucket.MODS -> modsBucket
        StorageBucket.IMAGES -> imagesBucket
      }
}
