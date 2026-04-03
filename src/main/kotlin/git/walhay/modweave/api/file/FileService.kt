package git.walhay.modweave.api.file

import git.walhay.modweave.api.file.repository.FileRepository
import git.walhay.modweave.api.storage.ISimpleStorageService
import git.walhay.modweave.api.version.Version
import git.walhay.modweave.api.version.VersionId
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
@Transactional
class FileService(
    private val simpleStorageService: ISimpleStorageService,
    private val fileRepository: FileRepository
) : IFileService {

  override fun uploadVersionFiles(version: Version, files: List<MultipartFile>) {
    val uploadedFiles = mutableListOf<String>()
    try {
      for (file in files) {
        val filename = "${version.modId}/${version.name}/${file.originalFilename}"
        uploadedFiles.add(simpleStorageService.uploadVersionFile(filename, file))

        val file =
            fileRepository.save(File(file.originalFilename!!, filename, VersionId(version.id)))
        version.files.addFirst(file)
      }
    } catch (e: Exception) {
      for (filename in uploadedFiles) {
        simpleStorageService.removeVersionFile(filename)
      }
    }
  }
}
