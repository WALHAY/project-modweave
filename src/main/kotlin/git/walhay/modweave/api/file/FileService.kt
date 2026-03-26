package git.walhay.modweave.api.file

import git.walhay.modweave.api.storage.ISimpleStorageService
import git.walhay.modweave.api.version.Version
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
@Transactional
class FileService(private val simpleStorageService: ISimpleStorageService) : IFileService {

  fun incrementDownloadCounter() {}

  override fun uploadFilesTransient(version: Version, files: List<MultipartFile>) {
    for (file in files) {
      val filename = "${version.mod.name}/${version.name}/${file.originalFilename}"
      simpleStorageService.uploadVersionFile(filename, file)

      version.files.addFirst(File(file.name, filename, version))
    }
  }
}
