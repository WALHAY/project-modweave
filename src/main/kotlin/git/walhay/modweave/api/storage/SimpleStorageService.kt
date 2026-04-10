package git.walhay.modweave.api.storage

import io.minio.*
import mu.KLogger
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class SimpleStorageService(
    private val minioClient: MinioClient,
    @param:Value($$"${minio.buckets.mods}") private val modsBucket: String,
    @param:Value($$"${minio.buckets.images}") private val imagesBucket: String,
    private val logger: KLogger = KotlinLogging.logger {}
) : ISimpleStorageService {

  @EventListener(ApplicationReadyEvent::class)
  fun initBuckets() {
    checkBucketExistence(modsBucket)
    checkBucketExistence(imagesBucket)
  }

  private fun checkBucketExistence(bucket: String) {
    logger.info("Checking bucket $bucket existence")
    if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
      logger.info("Bucket $bucket is missing")
      minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).objectLock(false).build())

      val policy: String =
          """
          {
            "Version": "2012-10-17",
            "Statement": [{
              "Effect": "Allow",
              "Principal": "*",
              "Action": ["s3:GetObject"],
              "Resource": ["arn:aws:s3:::%s/*"]
            }]
          }
          """
              .trimIndent()
              .format(bucket)

      minioClient.setBucketPolicy(
          SetBucketPolicyArgs.builder().bucket(bucket).config(policy).build())
      logger.info("Creating bucket $bucket")
    }
  }

  private fun putFileIntoBucket(bucket: String, filename: String, file: MultipartFile): String {
    logger.info(
        "Uploading file=${file.originalFilename} into bucket=$bucket with filename=$filename")
    try {
      minioClient.putObject(
          PutObjectArgs.builder()
              .bucket(bucket)
              .`object`(filename)
              .stream(file.inputStream, file.size, -1)
              .contentType(file.contentType)
              .build())

      return filename
    } catch (e: Exception) {
      logger.error(
          "Failed to upload file=${file.originalFilename} into bucket=$bucket with filename=$filename")
      throw e
    }
  }

  private fun removeFileFromBucket(bucket: String, filename: String) {
    minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).`object`(filename).build())
  }

  override fun uploadImage(filename: String, file: MultipartFile): String {
    return putFileIntoBucket(imagesBucket, filename, file)
  }

  override fun uploadVersionFile(filename: String, file: MultipartFile): String {
    return putFileIntoBucket(modsBucket, filename, file)
  }

  override fun removeVersionFile(filename: String) {
    removeFileFromBucket(modsBucket, filename)
  }

  override fun removeImage(filename: String) {
    removeFileFromBucket(modsBucket, filename)
  }
}
