package git.walhay.modweave.api.version

import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.version.dto.VersionUploadDto

interface IVersionService {
  fun uploadModVersion(mod: Mod, versionUploadDTO: VersionUploadDto): Version

  fun uploadModVersion(modId: ModId, versionUploadDTO: VersionUploadDto): Version

  fun deleteModVersion(modId: ModId, version: VersionId)
}
