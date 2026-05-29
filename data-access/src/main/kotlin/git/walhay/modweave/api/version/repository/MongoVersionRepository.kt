package git.walhay.modweave.api.version.repository

import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.version.Version
import git.walhay.modweave.api.version.VersionId
import java.util.UUID
import org.springframework.context.annotation.Profile
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Document(collection = "mod_versions")
data class VersionDocument(
    @Id val id: UUID,
    val name: String,
    val changes: String? = null,
    val uploadDate: java.time.LocalDateTime,
    val status: String,
    val modId: String,
)

interface SpringDataMongoVersionRepository : MongoRepository<VersionDocument, UUID> {
  fun findByModId(modId: String, pageable: Pageable): Page<VersionDocument>
}

@Repository
@Profile("mongodb")
class MongoVersionRepository(private val repository: SpringDataMongoVersionRepository) :
    VersionRepository {
  private fun VersionDocument.toDomain(): Version =
      Version(
          VersionId(this.id),
          this.name,
          this.changes,
          this.uploadDate,
          git.walhay.modweave.api.version.VersionStatus.valueOf(this.status),
          ModId(this.modId),
          mutableListOf())

  private fun Version.toDocument(): VersionDocument =
      VersionDocument(
          this.id.value,
          this.name,
          this.changes,
          this.uploadDate,
          this.status.name,
          this.modId.value)

  override fun findVersionById(versionId: VersionId): Version? =
      repository.findById(versionId.value).map { it.toDomain() }.orElse(null)

  override fun findVersionsByModId(modId: ModId, pageable: Pageable): Page<Version> =
      repository.findByModId(modId.value, pageable).map { it.toDomain() }

  override fun save(version: Version): Version = repository.save(version.toDocument()).toDomain()

  override fun delete(versionId: VersionId) = repository.deleteById(versionId.value)
}
