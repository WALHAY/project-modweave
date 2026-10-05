package git.walhay.modweave.api.version

import git.walhay.modweave.api.common.paging.Page
import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.mod.command.ModCreateCommand
import git.walhay.modweave.api.user.UserId
import git.walhay.modweave.api.version.command.VersionCreateCommand
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
