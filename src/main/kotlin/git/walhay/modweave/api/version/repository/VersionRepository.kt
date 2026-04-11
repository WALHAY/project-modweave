package git.walhay.modweave.api.version.repository

import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.version.Version
import git.walhay.modweave.api.version.VersionId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface VersionRepository {

  fun findVersionById(versionId: VersionId): Version?

  fun findVersionsByModId(modId: ModId, pageable: Pageable): Page<Version>

  fun save(version: Version): Version

  fun delete(versionId: VersionId)
}
