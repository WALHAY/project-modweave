package git.walhay.modweave.service

import io.minio.BucketExistsArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class SimpleStorageService(
    private val minioClient: MinioClient,
    @Value($$"${minio.buckets.mods}") private val modsBucket: String,
    @Value($$"${minio.buckets.images}") private val imagesBucket: String
) {

  private fun checkBucketExistence(bucket: String) {
    if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
      minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build())
    }
  }

  private fun putFileIntoBucket(bucket: String, filename: String, file: MultipartFile) {
    checkBucketExistence(bucket)

    minioClient.putObject(
        PutObjectArgs.builder()
            .bucket(bucket)
            .`object`(filename)
            .stream(file.inputStream, file.size, -1)
            .contentType(file.contentType)
            .build())
  }

  fun uploadImage(filename: String, file: MultipartFile) {
    putFileIntoBucket(imagesBucket, filename, file)
  }

  fun uploadVersionFile(filename: String, file: MultipartFile) {
    putFileIntoBucket(modsBucket, filename, file)
  }
}
