package git.walhay.modweave.api.version.http

import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.version.IVersionService
import git.walhay.modweave.api.version.http.dto.VersionResponseDto
import git.walhay.modweave.api.version.http.dto.VersionUploadDto
import git.walhay.modweave.api.version.http.dto.fromVersion
import git.walhay.modweave.api.version.http.dto.toVersionCreateCommand
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/mods/{modId}/versions")
class VersionController(
    private val versionService: IVersionService,
) {

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  fun uploadModVersion(
      @PathVariable modId: ModId,
      @Valid @ModelAttribute dto: VersionUploadDto
  ): VersionResponseDto =
      versionService.createModVersion(modId, dto.toVersionCreateCommand()).let {
        VersionResponseDto.fromVersion(it)
      }

  @DeleteMapping("/{versionName}")
  fun deleteVersion(@PathVariable modId: String, @PathVariable versionName: String) {
    deleteVersion(modId, versionName)
  }
}
