package git.walhay.modweave.api.file.repository

import git.walhay.modweave.api.file.File
import git.walhay.modweave.api.file.FileId
import git.walhay.modweave.api.version.VersionId
import git.walhay.modweave.mongo.SequenceService
import org.springframework.context.annotation.Profile
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Document(collection = "mod_files")
data class FileDocument(
    @Id val id: Long,
    val filename: String,
    val filePath: String,
    val downloads: Int = 0,
    val versionId: java.util.UUID,
)

interface SpringDataMongoFileRepository : MongoRepository<FileDocument, Long> {
  fun findByFilePath(filePath: String): FileDocument?

  fun findAllByVersionId(versionId: java.util.UUID): List<FileDocument>
}

@Repository
@Profile("mongodb")
class MongoFileRepository(
    private val repository: SpringDataMongoFileRepository,
    private val seq: SequenceService
) : git.walhay.modweave.api.file.repository.FileRepository {
  private fun FileDocument.toDomain(): File =
      File(FileId(this.id), this.filename, this.filePath, this.downloads, VersionId(this.versionId))

  private fun File.toDocument(): FileDocument {
    val id = if (this.id.value == 0L) seq.nextId("file_seq") else this.id.value
    return FileDocument(id, this.filename, this.filePath, this.downloads, this.versionId.value)
  }

  override fun save(file: File): File = repository.save(file.toDocument()).toDomain()

  override fun findById(id: FileId): File? =
      repository.findById(id.value).map { it.toDomain() }.orElse(null)

  override fun findByFilePath(filePath: String): File? =
      repository.findByFilePath(filePath)?.toDomain()

  override fun findAllByVersionId(versionId: VersionId): List<File> =
      repository.findAllByVersionId(versionId.value).map { it.toDomain() }

  override fun deleteById(id: FileId) = repository.deleteById(id.value)
}
