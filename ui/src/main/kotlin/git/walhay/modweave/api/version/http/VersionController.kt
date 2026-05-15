package git.walhay.modweave.api.version.http

import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.version.IVersionService
import git.walhay.modweave.api.version.VersionId
import git.walhay.modweave.api.version.http.dto.VersionResponseDto
import git.walhay.modweave.api.version.http.dto.VersionUploadDto
import jakarta.validation.Valid
import mu.KLogger
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/mods/{modId}/versions")
class VersionController(
    private val versionService: IVersionService,
) {
  private val logger: KLogger = KotlinLogging.logger {}

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  fun uploadModVersion(
      @PathVariable modId: ModId,
      @Valid @ModelAttribute dto: VersionUploadDto,
  ): VersionResponseDto {
    logger.info { "POST /mods/$modId/versions - uploading version: ${dto.name}" }
    return versionService.createModVersion(modId, dto.toVersionCreateCommand()).let {
      VersionResponseDto.fromVersion(it)
    }
  }

  @DeleteMapping("/{versionName}")
  fun deleteVersion(
      @PathVariable modId: String,
      @PathVariable versionName: String,
  ) {
    logger.info { "DELETE /mods/$modId/versions/$versionName" }
    versionService.deleteModVersion(VersionId(UUID.fromString(versionName)))
  }
}
