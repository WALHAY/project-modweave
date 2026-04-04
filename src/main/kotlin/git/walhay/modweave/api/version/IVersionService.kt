package git.walhay.modweave.api.version

import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.mod.command.ModCreateCommand
import git.walhay.modweave.api.version.command.VersionCreateCommand

interface IVersionService {
  fun uploadModVersion(mod: Mod, command: ModCreateCommand): Version

  fun uploadModVersion(modId: ModId, command: VersionCreateCommand): Version

  fun deleteModVersion(modId: ModId, versionId: VersionId)
}
