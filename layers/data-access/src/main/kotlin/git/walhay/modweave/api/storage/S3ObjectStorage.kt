package git.walhay.modweave.api.storage

import io.minio.*
import java.io.InputStream
import mu.KLogger
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class S3ObjectStorage(
    private val minioClient: MinioClient,
    @param:Value($$"${storage.s3.buckets.mods}") private val modsBucket: String,
    @param:Value($$"${storage.s3.buckets.images}") private val imagesBucket: String,
    private val logger: KLogger = KotlinLogging.logger {},
) : ObjectStorageClient {
  @EventListener(ApplicationReadyEvent::class)
  fun initBuckets() {
    checkBucketExistence(modsBucket)
    checkBucketExistence(imagesBucket)
  }

  private fun checkBucketExistence(bucket: String) {
    logger.info("Checking bucket $bucket existence")
    if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
      logger.info("Bucket $bucket is missing")
      minioClient.makeBucket(
          MakeBucketArgs.builder().bucket(bucket).objectLock(false).build(),
      )

      logger.info("Creating bucket $bucket")
    }

    if (bucket == modsBucket) {
      minioClient.deleteBucketPolicy(DeleteBucketPolicyArgs.builder().bucket(bucket).build())
    } else {
      val policy = """
          {"Version":"2012-10-17","Statement":[{
            "Effect":"Allow","Principal":"*","Action":["s3:GetObject"],
            "Resource":["arn:aws:s3:::$bucket/*"]
          }]}
          """.trimIndent()
      minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucket).config(policy).build())
    }
  }

  private fun putFileIntoBucket(
      bucket: String,
      filename: String,
      file: MultipartFile,
  ): String {
    logger.info(
        "Uploading file=${file.originalFilename} into bucket=$bucket with filename=$filename")
    try {
      file.inputStream.use { input ->
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucket)
                .`object`(filename)
                .stream(input, file.size, -1)
                .contentType(file.contentType ?: "application/octet-stream")
                .build(),
        )
      }

      return filename
    } catch (e: Exception) {
      logger.error(
          "Failed to upload file=${file.originalFilename} into bucket=$bucket with filename=$filename")
      throw e
    }
  }

  private fun removeFileFromBucket(
      bucket: String,
      filename: String,
  ) {
    minioClient.removeObject(
        RemoveObjectArgs.builder().bucket(bucket).`object`(filename).build(),
    )
  }

  override fun upload(bucket: StorageBucket, filename: String, file: MultipartFile): String =
      putFileIntoBucket(bucketName(bucket), filename, file)

  override fun remove(bucket: StorageBucket, filename: String) {
    removeFileFromBucket(bucketName(bucket), filename)
  }

  override fun downloadVersionFile(filename: String): InputStream =
      minioClient.getObject(GetObjectArgs.builder().bucket(modsBucket).`object`(filename).build())

  private fun bucketName(bucket: StorageBucket): String = when (bucket) {
    StorageBucket.MODS -> modsBucket
    StorageBucket.IMAGES -> imagesBucket
  }
}
