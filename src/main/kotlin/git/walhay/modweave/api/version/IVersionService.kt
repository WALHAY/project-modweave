package git.walhay.modweave.api.version

import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.api.version.dto.VersionUploadDto
import org.springframework.web.multipart.MultipartFile

interface IVersionService {
  fun uploadModVersion(mod: Mod, modVersionUploadDTO: VersionUploadDto): Version

  fun uploadModVersion(modId: String, modVersionUploadDTO: VersionUploadDto): Version

  fun uploadModVersionTransient(
      mod: Mod,
      name: String,
      changes: String?,
      files: List<MultipartFile>
  ): Version

  fun uploadModVersionTransient(mod: Mod, name: String, files: List<MultipartFile>): Version

  fun deleteModVersion(modId: String, version: String)
}
