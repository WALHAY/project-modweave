package git.walhay.modweave.service

import git.walhay.modweave.model.File
import git.walhay.modweave.model.Version
import git.walhay.modweave.repository.FileRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
@Transactional
class FileService(
    private val fileRepository: FileRepository,
    private val simpleStorageService: SimpleStorageService
) {

  fun uploadNewFiles(version: Version, files: List<MultipartFile>) {
    for (file in files) {
      val filename = "${version.mod.name}/${version.name}/${file.originalFilename}"
      simpleStorageService.uploadVersionFile(filename, file)

      val file = File(file.name, filename, version)
      fileRepository.save(file)
    }
  }
}
