package git.walhay.modweave.api.version.repository

import git.walhay.modweave.api.common.paging.Page
import git.walhay.modweave.api.common.paging.toDomainPage
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.mod.repository.ModEntity
import git.walhay.modweave.api.version.Version
import git.walhay.modweave.api.version.VersionId
import git.walhay.modweave.api.version.VersionStatus
import jakarta.persistence.EntityManager
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class JpaVersionRepository(
    private val repository: SpringDataVersionRepository,
    private val entityManager: EntityManager,
) : VersionRepository {
  override fun save(version: Version): Version =
      repository.save(VersionEntity.fromVersion(version)).toDomain()

  override fun findVersionById(versionId: VersionId): Version? =
      repository.findByIdOrNull(versionId.value)?.toDomain()

  override fun findVersionsByModId(
      modId: ModId,
      pageable: Pageable,
  ): Page<Version> = repository.findAllByModId(modId.value, pageable).toDomainPage { it.toDomain() }

  override fun findVersionsByModIdAndStatus(
      modId: ModId,
      status: VersionStatus,
      pageable: Pageable,
  ): Page<Version> =
      repository.findAllByModIdAndStatus(modId.value, status, pageable).toDomainPage {
        it.toDomain()
      }

  override fun delete(versionId: VersionId) {
    val version = repository.findByIdOrNull(versionId.value) ?: return
    // Keep the managed parent collection consistent before Hibernate checks deleted references.
    entityManager.find(ModEntity::class.java, version.modId)?.versions?.removeIf {
      it.id == versionId.value
    }
    repository.delete(version)
  }
}
