package git.walhay.modweave.api.version.repository

import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.version.Version
import git.walhay.modweave.api.version.VersionId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class JpaVersionRepository(private val repository: SpringDataVersionRepository) :
    VersionRepository {
  override fun save(version: Version): Version =
      repository.save<VersionEntity>(VersionEntity.fromVersion(version)).toDomain()

  override fun findVersionsByModId(modId: ModId, pageable: Pageable): Page<Version> =
      repository.findAllByModId(modId.value, pageable).map { it.toDomain() }

  override fun delete(versionId: VersionId) = repository.deleteById(versionId.value)
}
