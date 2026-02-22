package git.walhay.modweave.controller

import git.walhay.modweave.dto.VersionUploadDTO
import git.walhay.modweave.model.Version
import git.walhay.modweave.service.ModService
import git.walhay.modweave.service.VersionService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/mods/{modId}")
class VersionController(
    private val versionService: VersionService,
    private val modService: ModService
) {

  @PostMapping
  fun uploadModVersion(
      @PathVariable modId: String,
      @Valid @ModelAttribute versionUploadDTO: VersionUploadDTO
  ): Version {
    val mod = modService.findModById(modId)
    return versionService.uploadModVersion(mod, versionUploadDTO)
  }

  @DeleteMapping("/{versionName}")
  fun deleteVersion(@PathVariable modId: String, @PathVariable versionName: String) {}
}
