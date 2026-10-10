package git.walhay.modweave.domain.version

import git.walhay.modweave.domain.common.paging.Page
import git.walhay.modweave.domain.mod.ModId
import org.springframework.data.domain.Pageable

interface VersionRepository {
  fun findVersionById(versionId: VersionId): Version?

  fun findVersionsByModId(
      modId: ModId,
      pageable: Pageable,
  ): Page<Version>

  fun findVersionsByModIdAndStatus(
      modId: ModId,
      status: VersionStatus,
      pageable: Pageable
  ): Page<Version>

  fun save(version: Version): Version

  fun delete(versionId: VersionId)
}
