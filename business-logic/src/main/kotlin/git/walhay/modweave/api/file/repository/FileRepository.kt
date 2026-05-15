package git.walhay.modweave.api.file.repository

import git.walhay.modweave.api.file.File
import git.walhay.modweave.api.file.FileId
import git.walhay.modweave.api.version.VersionId

interface FileRepository {
  fun save(file: File): File

  fun findById(id: FileId): File?

  fun findByFilePath(filePath: String): File?

  fun findAllByVersionId(versionId: VersionId): List<File>

  fun deleteById(id: FileId)
}
