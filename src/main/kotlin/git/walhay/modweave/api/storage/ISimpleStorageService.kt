package git.walhay.modweave.api.storage

import org.springframework.web.multipart.MultipartFile

interface ISimpleStorageService {
  fun uploadImage(filename: String, file: MultipartFile): String

  fun uploadVersionFile(filename: String, file: MultipartFile): String

  fun removeVersionFile(filename: String)
}
