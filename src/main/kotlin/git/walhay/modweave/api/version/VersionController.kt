package git.walhay.modweave.api.version

import git.walhay.modweave.api.version.dto.VersionResponseDto
import git.walhay.modweave.api.version.dto.VersionUploadDto
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/mods/{modId}")
class VersionController(
    private val versionService: IVersionService,
) {

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  fun uploadModVersion(
      @PathVariable modId: String,
      @Valid @ModelAttribute versionUploadDTO: VersionUploadDto
  ): VersionResponseDto =
      versionService.uploadModVersion(modId, versionUploadDTO).toVersionResponseDto()

  @DeleteMapping("/{versionName}")
  fun deleteVersion(@PathVariable modId: String, @PathVariable versionName: String) {
    deleteVersion(modId, versionName)
  }
}
