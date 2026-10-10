package git.walhay.modweave.domain.file

import git.walhay.modweave.domain.version.VersionId

interface FileRepository {
  fun save(file: File): File

  fun findById(id: FileId): File?

  fun findByFilePath(filePath: String): File?

  fun findAllByVersionId(versionId: VersionId): List<File>

  fun incrementDownloads(id: FileId)

  fun deleteById(id: FileId)
}
