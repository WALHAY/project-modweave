package git.walhay.modweave.api.file.repository

import git.walhay.modweave.api.file.File
import git.walhay.modweave.api.file.FileId
import git.walhay.modweave.api.version.VersionId
import org.springframework.stereotype.Repository

@Repository
class JpaFileRepository(private val repository: SpringDataFileRepository) : FileRepository {
  override fun save(file: File): File = repository.save(FileEntity.fromFile(file)).toDomain()

  override fun findById(id: FileId): File? = repository.findById(id.value).orElse(null)?.toDomain()

  override fun findByFilePath(filePath: String): File? = repository.findByFilePath(filePath)?.toDomain()

  override fun findAllByVersionId(versionId: VersionId): List<File> =
      repository.findAllByVersionId(versionId.value).map { it.toDomain() }

  override fun deleteById(id: FileId) = repository.deleteById(id.value)
}
