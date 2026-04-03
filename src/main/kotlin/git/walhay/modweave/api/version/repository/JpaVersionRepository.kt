package git.walhay.modweave.api.version.repository

import git.walhay.modweave.api.version.Version
import git.walhay.modweave.api.version.toEntity
import org.springframework.stereotype.Repository

@Repository
class JpaVersionRepository(private val repository: SpringDataVersionRepository) :
    VersionRepository {
  override fun save(version: Version): Version =
      repository.save<VersionEntity>(version.toEntity()).toModel()

  override fun delete(version: Version) = repository.delete(version.toEntity())
}
