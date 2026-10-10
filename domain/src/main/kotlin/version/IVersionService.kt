package git.walhay.modweave.domain.version

import git.walhay.modweave.domain.common.paging.Page
import git.walhay.modweave.domain.mod.Mod
import git.walhay.modweave.domain.mod.ModCreateCommand
import git.walhay.modweave.domain.mod.ModId
import git.walhay.modweave.domain.user.UserId
import org.springframework.data.domain.Pageable

interface IVersionService {
  fun getModVersion(versionId: VersionId): Version

  fun getModVersions(
      userId: UserId?,
      modId: ModId,
      pageable: Pageable,
  ): Page<Version>

  fun createModVersion(
      mod: Mod,
      command: ModCreateCommand,
  ): Version

  fun createModVersion(
      modId: ModId,
      command: VersionCreateCommand,
  ): Version

  fun changeVersionStatus(
      userId: UserId,
      modId: ModId,
      versionId: VersionId,
      status: VersionStatus
  ): Version

  fun deleteModVersion(modId: ModId, versionId: VersionId)
}
