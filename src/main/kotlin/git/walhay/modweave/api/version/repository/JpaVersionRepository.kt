package git.walhay.modweave.api.version.repository

import git.walhay.modweave.api.version.Version
import git.walhay.modweave.api.version.VersionId
import org.springframework.stereotype.Repository

@Repository
class JpaVersionRepository(private val repository: SpringDataVersionRepository) :
    VersionRepository {
  override fun save(version: Version): Version =
      repository.save<VersionEntity>(VersionEntity.fromVersion(version)).toDomain()

  override fun delete(versionId: VersionId) = repository.deleteById(versionId.value)
}
