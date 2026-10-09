package git.walhay.modweave.api.file

import java.io.OutputStream
import java.time.LocalDateTime

interface IFileDownloadService {
  fun getDownload(id: FileId): FileDownload

  fun writeDownload(download: FileDownload, output: OutputStream)
}

data class FileDownload(val file: File, val uploadedAt: LocalDateTime)

class FileNotFoundException(id: FileId) : RuntimeException("File not found: $id")
