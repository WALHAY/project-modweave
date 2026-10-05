package git.walhay.modweave.api.storage

import java.io.InputStream
import org.springframework.web.multipart.MultipartFile

enum class StorageBucket {
  MODS,
  IMAGES
}

/** Low-level S3 operations. Application services use ISimpleStorageService instead. */
interface ObjectStorageClient {
  fun upload(bucket: StorageBucket, filename: String, file: MultipartFile): String

  fun remove(bucket: StorageBucket, filename: String)

  fun downloadVersionFile(filename: String): InputStream
}
