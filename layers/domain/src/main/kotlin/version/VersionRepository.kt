package git.walhay.modweave.domain.version

import git.walhay.modweave.domain.common.paging.Page
import git.walhay.modweave.domain.common.paging.PageRequest
import git.walhay.modweave.domain.mod.ModId

interface VersionRepository {
  fun findVersionById(versionId: VersionId): Version?

  fun findVersionsByModId(
      modId: ModId,
      pageRequest: PageRequest,
  ): Page<Version>

  fun findVersionsByModIdAndStatus(
      modId: ModId,
      status: VersionStatus,
      pageRequest: PageRequest
  ): Page<Version>

  fun save(version: Version): Version

  fun delete(versionId: VersionId)
}
