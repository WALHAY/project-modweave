package git.walhay.modweave.api.version

import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.mod.command.ModCreateCommand
import git.walhay.modweave.api.version.command.VersionCreateCommand
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface IVersionService {
    fun getModVersions(modId: ModId, pageable: Pageable): Page<Version>
  fun createModVersion(mod: Mod, command: ModCreateCommand): Version

  fun createModVersion(modId: ModId, command: VersionCreateCommand): Version

  fun deleteModVersion(modId: ModId, versionId: VersionId)
}
