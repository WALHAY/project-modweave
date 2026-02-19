package git.walhay.modweave.services

import git.walhay.modweave.models.File
import git.walhay.modweave.models.Version
import git.walhay.modweave.repositories.FileRepository
import io.minio.BucketExistsArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
@Transactional
class FileService @Autowired constructor(
    private val fileRepository: FileRepository,
    private val minioClient: MinioClient
) {

    fun uploadNewFiles(version: Version, files: List<MultipartFile>) {
        val bucket = version.bucketKey
        if(!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build())
        }

        for(file in files) {
            val filename = "${version.mod.name}/${version.versionName}/${file.originalFilename}"
            minioClient.putObject(PutObjectArgs.builder().contentType(file.contentType).stream(file.inputStream, file.size, -1).bucket(bucket).`object`(filename).build())

            val file = File(null, filename, version)
            fileRepository.save(file)
        }
    }
}