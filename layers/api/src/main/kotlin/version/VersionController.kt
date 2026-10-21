package git.walhay.modweave.api.version

import git.walhay.modweave.api.common.apiLink
import git.walhay.modweave.domain.mod.ModId
import git.walhay.modweave.domain.user.UserId
import git.walhay.modweave.domain.version.IVersionService
import git.walhay.modweave.domain.version.VersionId
import git.walhay.modweave.domain.version.VersionNotFoundException
import git.walhay.modweave.domain.version.VersionStatus
import jakarta.validation.Valid
import java.util.UUID
import mu.KLogger
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.http.ResponseEntity
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

  @PostMapping(version = "1", consumes = ["multipart/form-data"])
  @ResponseStatus(HttpStatus.CREATED)
  fun uploadModVersion(
      @PathVariable modId: String,
      @Valid @ModelAttribute dto: VersionUploadDto,
  ): VersionResponseDto<FileResponseDto> {
    logger.info { "POST /mods/$modId/versions - uploading version: ${dto.name}" }
    return versionService.createModVersion(ModId(modId), dto.toVersionCreateCommand()).let {
      VersionResponseDto.fromVersion(it)
    }
  }

  @PostMapping(version = "2", consumes = ["multipart/form-data"])
  fun createVersion(
      @PathVariable modId: String,
      @Valid @ModelAttribute dto: VersionUploadDto,
  ): ResponseEntity<VersionResponseDto<FileResponseDto>> {
    val result = uploadModVersion(modId, dto)
    return ResponseEntity.created(
            apiLink(VersionController::getVersion, modId, result.id).link.toUri())
        .body(result)
  }

  @GetMapping("/{versionId}", version = "2")
  fun getVersion(
      @PathVariable modId: String,
      @PathVariable versionId: UUID
  ): VersionResponseDto<FileResponseDto> {
    val version = versionService.getModVersion(VersionId(versionId))
    if (version.modId != ModId(modId)) throw VersionNotFoundException(VersionId(versionId))
    return VersionResponseDto.fromVersion(version)
  }

  @PatchMapping("/{versionId}", version = "1")
  fun changeVersionStatus(
      @PathVariable modId: String,
      @PathVariable versionId: UUID,
      @RequestParam status: VersionStatus,
      @AuthenticationPrincipal user: UserDetails?,
  ): VersionResponseDto<FileResponseDto> {
    val authUser = requireUser(user)
    logger.info { "PATCH /mods/$modId/versions/$versionId" }
    return versionService
        .changeVersionStatus(UserId(authUser.username), ModId(modId), VersionId(versionId), status)
        .let { VersionResponseDto.fromVersion(it) }
  }

  // Let the body converter reject unsupported media after version selection. A consumes
  // condition would allow MVC to select the legacy query-based handler and return 400.
  @PatchMapping("/{versionId}", version = "2")
  fun moderateVersion(
      @PathVariable modId: String,
      @PathVariable versionId: UUID,
      @RequestBody dto: VersionModerationDto,
      @AuthenticationPrincipal user: UserDetails?,
  ): VersionResponseDto<FileResponseDto> =
      changeVersionStatus(modId, versionId, dto.status.versionStatus, user)

  @DeleteMapping("/{versionId}", version = "1")
  fun deleteVersion(
      @PathVariable modId: String,
      @PathVariable versionId: UUID,
  ) {
    logger.info { "DELETE /mods/$modId/versions/$versionId" }
    versionService.deleteModVersion(ModId(modId), VersionId(versionId))
  }

  @DeleteMapping("/{versionId}", version = "2")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun removeVersion(@PathVariable modId: String, @PathVariable versionId: UUID) =
      deleteVersion(modId, versionId)

  private fun requireUser(user: UserDetails?): UserDetails =
      user ?: throw ResponseStatusException(UNAUTHORIZED, "Authentication required")
}
