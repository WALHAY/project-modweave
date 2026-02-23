package git.walhay.modweave.service

import io.minio.BucketExistsArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import jakarta.annotation.PostConstruct
import mu.KLogger
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class SimpleStorageService(
    private val minioClient: MinioClient,
    @Value($$"${minio.buckets.mods}") private val modsBucket: String,
    @Value($$"${minio.buckets.images}") private val imagesBucket: String,
    private val logger: KLogger = KotlinLogging.logger { }
) {

    @PostConstruct
    fun initBuckets() {
        checkBucketExistence(modsBucket)
        checkBucketExistence(imagesBucket)
    }

  private fun checkBucketExistence(bucket: String) {
      logger.info("Checking bucket $bucket existence")
    if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
        logger.info("Bucket $bucket is missing")
      minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).objectLock(false).build())
        logger.info("Creating bucket $bucket")
    }
  }

  private fun putFileIntoBucket(bucket: String, filename: String, file: MultipartFile) {
      logger.info("Uploading file=${file.originalFilename} into bucket=$bucket with filename=$filename")
      try {
          minioClient.putObject(
              PutObjectArgs.builder()
                  .bucket(bucket)
                  .`object`(filename)
                  .stream(file.inputStream, file.size, -1)
                  .contentType(file.contentType)
                  .build()
          )
      } catch (e: Exception) {
          logger.error("Failed to upload file=${file.originalFilename} into bucket=$bucket with filename=$filename")
      }
  }

  fun uploadImage(filename: String, file: MultipartFile) {
    putFileIntoBucket(imagesBucket, filename, file)
  }

  fun uploadVersionFile(filename: String, file: MultipartFile) {
    putFileIntoBucket(modsBucket, filename, file)
  }
}
