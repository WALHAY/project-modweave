package git.walhay.modweave.api.version.repository

import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.version.Version
import git.walhay.modweave.api.version.VersionId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface VersionRepository {
  fun save(version: Version): Version

    fun findVersionsByModId(modId: ModId, pageable: Pageable): Page<Version>

  fun delete(versionId: VersionId)
}
