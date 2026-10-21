package git.walhay.modweave.domain.storage

import java.io.InputStream
import org.springframework.web.multipart.MultipartFile

interface ISimpleStorageService {
  fun downloadImage(filename: String): InputStream

  fun downloadVersionFile(filename: String): InputStream

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
}
