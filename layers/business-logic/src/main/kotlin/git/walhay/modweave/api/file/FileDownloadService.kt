package git.walhay.modweave.api.file

import git.walhay.modweave.api.file.repository.FileRepository
import git.walhay.modweave.api.storage.ISimpleStorageService
import git.walhay.modweave.api.version.IVersionService
import java.io.OutputStream
import org.springframework.cache.annotation.CacheEvict
import org.springframework.stereotype.Service

@Service
class FileDownloadService(
    private val files: FileRepository,
    private val versions: IVersionService,
    private val storage: ISimpleStorageService,
) : IFileDownloadService {
  override fun getDownload(id: FileId): FileDownload {
    val file = files.findById(id) ?: throw FileNotFoundException(id)
    // VersionService checks visibility before any conditional response or storage access.
    val version = versions.getModVersion(file.versionId)
    return FileDownload(file, version.uploadDate)
  }

  @CacheEvict("versions", allEntries = true)
  override fun writeDownload(download: FileDownload, output: OutputStream) {
    storage.downloadVersionFile(download.file.filePath).use { it.copyTo(output) }
    files.incrementDownloads(download.file.id)
  }
}
