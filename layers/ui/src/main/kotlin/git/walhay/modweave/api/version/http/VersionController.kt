package git.walhay.modweave.api.version.http

import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.user.UserId
import git.walhay.modweave.api.version.IVersionService
import git.walhay.modweave.api.version.VersionId
import git.walhay.modweave.api.version.VersionStatus
import git.walhay.modweave.api.version.exception.VersionNotFoundException
import git.walhay.modweave.api.version.http.dto.VersionResponseDto
import git.walhay.modweave.api.version.http.dto.VersionUploadDto
import jakarta.validation.Valid
import java.net.URI
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
  ): VersionResponseDto {
    logger.info { "POST /mods/$modId/versions - uploading version: ${dto.name}" }
    return versionService.createModVersion(ModId(modId), dto.toVersionCreateCommand()).let {
      VersionResponseDto.fromVersion(it)
    }
  }

  @PostMapping(version = "2", consumes = ["multipart/form-data"])
  fun createVersion(
      @PathVariable modId: String,
      @Valid @ModelAttribute dto: VersionUploadDto,
  ): ResponseEntity<VersionResponseDto> {
    val result = uploadModVersion(modId, dto)
    return ResponseEntity.created(URI.create("/api/v2/mods/$modId/versions/${result.id}"))
        .body(result)
  }

  @GetMapping("/{versionId}", version = "2")
  fun getVersion(@PathVariable modId: String, @PathVariable versionId: UUID): VersionResponseDto {
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
  ): VersionResponseDto {
    val authUser = requireUser(user)
    logger.info { "PATCH /mods/$modId/versions/$versionId" }
    return versionService
        .changeVersionStatus(UserId(authUser.username), ModId(modId), VersionId(versionId), status)
        .let { VersionResponseDto.fromVersion(it) }
  }

  @PatchMapping("/{versionId}", version = "2")
  fun moderateVersion(
      @PathVariable modId: String,
      @PathVariable versionId: UUID,
      @RequestBody body: Map<String, Any?>,
      @AuthenticationPrincipal user: UserDetails?,
  ): VersionResponseDto {
    require(body.keys == setOf("status")) { "Exactly the status field is required" }
    val status = body["status"]
    require(status == "APPROVED" || status == "REJECTED") {
      "Moderation status must be APPROVED or REJECTED"
    }
    return changeVersionStatus(modId, versionId, VersionStatus.valueOf(status as String), user)
  }

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
