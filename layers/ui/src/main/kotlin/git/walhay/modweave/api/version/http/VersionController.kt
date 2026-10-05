package git.walhay.modweave.api.version.http

import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.user.UserId
import git.walhay.modweave.api.version.IVersionService
import git.walhay.modweave.api.version.VersionId
import git.walhay.modweave.api.version.VersionStatus
import git.walhay.modweave.api.version.http.dto.VersionResponseDto
import git.walhay.modweave.api.version.http.dto.VersionUploadDto
import jakarta.validation.Valid
import java.util.UUID
import mu.KLogger
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/mods/{modId}/versions")
class VersionController(
    private val versionService: IVersionService,
) {
  private val logger: KLogger = KotlinLogging.logger {}

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  fun uploadModVersion(
      @PathVariable modId: String,
      @Valid @ModelAttribute dto: VersionUploadDto,
  ): VersionResponseDto {
    logger.info { "POST /mods/$modId/versions - uploading version: ${dto.name}" }
    return versionService.createModVersion(ModId(modId), dto.toVersionCreateCommand()).let {
      VersionResponseDto.fromVersion(it)
    }
  }

  @PatchMapping("/{versionId}")
  fun changeVersionStatus(
      @PathVariable modId: String,
      @PathVariable versionId: UUID,
      @RequestParam status: VersionStatus,
      @AuthenticationPrincipal user: UserDetails?,
  ) {
    val authUser = requireUser(user)
    logger.info { "PATCH /mods/$modId/versions/$versionId" }
    versionService.changeVersionStatus(
        UserId(authUser.username), ModId(modId), VersionId(versionId), status)
  }

  @DeleteMapping("/{versionId}")
  fun deleteVersion(
      @PathVariable modId: String,
      @PathVariable versionId: UUID,
  ) {
    logger.info { "DELETE /mods/$modId/versions/$versionId" }
    versionService.deleteModVersion(ModId(modId), VersionId(versionId))
  }

  private fun requireUser(user: UserDetails?): UserDetails =
      user ?: throw ResponseStatusException(UNAUTHORIZED, "Authentication required")
}
