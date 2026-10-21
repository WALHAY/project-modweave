package git.walhay.modweave.domain.file

import git.walhay.modweave.domain.storage.ISimpleStorageService
import git.walhay.modweave.domain.version.VersionService
import java.io.OutputStream
import java.time.LocalDateTime
import org.springframework.cache.annotation.CacheEvict
import org.springframework.stereotype.Service

@Service
class FileDownloadService(
    private val files: FileRepository,
    private val versions: VersionService,
    private val storage: ISimpleStorageService,
) {
  fun getDownload(id: FileId): FileDownload {
    val file = files.findById(id) ?: throw FileNotFoundException(id)
    // VersionService checks visibility before any conditional response or storage access.
    val version = versions.getModVersion(file.versionId)
    return FileDownload(file, version.uploadDate)
  }

  @CacheEvict("versions", allEntries = true)
  fun writeDownload(download: FileDownload, output: OutputStream) {
    storage.downloadVersionFile(download.file.filePath).use { it.copyTo(output) }
    files.incrementDownloads(download.file.id)
  }
}

data class FileDownload(val file: File, val uploadedAt: LocalDateTime)

class FileNotFoundException(id: FileId) : RuntimeException("File not found: $id")
