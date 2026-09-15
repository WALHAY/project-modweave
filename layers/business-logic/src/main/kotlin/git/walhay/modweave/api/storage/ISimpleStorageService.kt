package git.walhay.modweave.api.storage

import java.io.InputStream
import org.springframework.web.multipart.MultipartFile

interface ISimpleStorageService {
  fun uploadImage(
      filename: String,
      file: MultipartFile,
  ): String

  fun uploadVersionFile(
      filename: String,
      file: MultipartFile,
  ): String

  fun removeVersionFile(filename: String)

  fun removeImage(filename: String)

  fun downloadVersionFile(filename: String): InputStream
}
