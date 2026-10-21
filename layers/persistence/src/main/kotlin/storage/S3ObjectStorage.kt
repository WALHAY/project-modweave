package git.walhay.modweave.persistence.storage

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
    }
    try {
      client.deleteBucketPolicy { it.bucket(modsBucket) }
    } catch (error: S3Exception) {
      if (error.awsErrorDetails()?.errorCode() != "NoSuchBucketPolicy") throw error
    }
    client.putBucketPolicy {
      it.bucket(imagesBucket)
          .policy(
              """
          {"Version":"2012-10-17","Statement":[{
            "Effect":"Allow","Principal":"*","Action":["s3:GetObject"],
            "Resource":["arn:aws:s3:::$imagesBucket/*"]
          }]}
          """
                  .trimIndent())
    }
  }

  override fun upload(bucket: StorageBucket, filename: String, file: MultipartFile): String {
    logger.info {
      "Uploading file=${file.originalFilename} into bucket=$bucket with filename=$filename"
    }
    var input: InputStream? = null
    val streams =
        ContentStreamProvider.fromInputStreamSupplier { file.inputStream.also { input = it } }
    AutoCloseable { input?.close() }
        .use {
          client.putObject(
              {
                it.bucket(bucketName(bucket))
                    .key(filename)
                    .contentType(file.contentType ?: "application/octet-stream")
              },
              RequestBody.fromContentProvider(
                  streams, file.size, file.contentType ?: "application/octet-stream"))
        }
    return filename
  }

  override fun remove(bucket: StorageBucket, filename: String) {
    client.deleteObject { it.bucket(bucketName(bucket)).key(filename) }
  }

  override fun downloadVersionFile(filename: String): InputStream =
      client.getObject { it.bucket(modsBucket).key(filename) }

  private fun bucketName(bucket: StorageBucket): String =
      when (bucket) {
        StorageBucket.MODS -> modsBucket
        StorageBucket.IMAGES -> imagesBucket
      }
}
