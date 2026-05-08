package git.walhay.modweave.api.file

import git.walhay.modweave.api.file.repository.FileRepository
import git.walhay.modweave.api.storage.ISimpleStorageService
import git.walhay.modweave.api.version.Version
import jakarta.transaction.Transactional
import mu.KLogger
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
@Transactional
class FileService(
    private val simpleStorageService: ISimpleStorageService,
    private val fileRepository: FileRepository,
) : IFileService {
  private val logger: KLogger = KotlinLogging.logger {}

  override fun uploadVersionFiles(
      version: Version,
      files: List<MultipartFile>,
  ) {
    logger.info { "Uploading ${files.size} files for version: ${version.name}" }
    val uploadedFiles = mutableListOf<String>()
    try {
      for (file in files) {
        val filename = "${version.modId}/${version.name}/${file.originalFilename}"
        logger.debug { "Uploading file: ${file.originalFilename}" }
        uploadedFiles.add(simpleStorageService.uploadVersionFile(filename, file))

        val savedFile = fileRepository.save(File(file.originalFilename!!, filename, version.id))
        version.files.addFirst(savedFile)
        logger.debug { "File saved to repository: ${savedFile.id}" }
      }
      logger.info {
        "Successfully uploaded ${uploadedFiles.size} files for version: ${version.name}"
      }
    } catch (e: Exception) {
      logger.warn { "File upload failed, rolling back uploaded files: ${e.message}" }
      for (filename in uploadedFiles) {
        simpleStorageService.removeVersionFile(filename)
      }
      throw e
    }
  }
}
