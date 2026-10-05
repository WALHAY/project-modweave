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
@Transactional(rollbackOn = [Exception::class])
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
    require(files.isNotEmpty()) { "Version must contain at least one file" }
    val filenames =
        files.map { file ->
          val name = file.originalFilename
          require(!file.isEmpty && !name.isNullOrBlank()) {
            "Uploaded file must have a name and content"
          }
          require(
              name != "." &&
                  name != ".." &&
                  name.none { it == '/' || it == '\\' || it.isISOControl() }) {
                "Invalid filename"
              }
          name
        }
    for ((index, file) in files.withIndex()) {
      val id = FileId()
      val filename = "${version.modId.value}/${version.id.value}/${id.value}/${filenames[index]}"
      logger.debug { "Uploading file: ${file.originalFilename}" }
      simpleStorageService.uploadVersionFile(filename, file)

      val savedFile =
          fileRepository.save(File(id, filenames[index], filename, versionId = version.id))
      version.files.addLast(savedFile)
      logger.debug { "File saved to repository: ${savedFile.id}" }
    }
    logger.info { "Successfully uploaded ${files.size} files for version: ${version.name}" }

  }
}
