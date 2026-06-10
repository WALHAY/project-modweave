package git.walhay.modweave.api.mod.http

import git.walhay.modweave.api.mod.IModService
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.mod.http.dto.ModResponseDto
import git.walhay.modweave.api.mod.http.dto.ModUploadDto
import git.walhay.modweave.api.user.UserId
import git.walhay.modweave.api.version.IVersionService
import git.walhay.modweave.api.version.http.dto.VersionResponseDto
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import mu.KLogger
import mu.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.web.SortDefault
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/mods")
class ModController(
    private val modService: IModService,
    private val versionService: IVersionService,
) {
  private val logger: KLogger = KotlinLogging.logger {}

  @GetMapping("/{modId}")
  fun getMod(
      @PathVariable modId: ModId,
  ): ModResponseDto {
    logger.info { "GET /mods/$modId" }
    return modService.findModById(modId).let { ModResponseDto.fromMod(it) }
  }

  @GetMapping
  fun getMods(
      @RequestParam page: Int,
      @RequestParam size: Int,
      @RequestParam(required = false) name: String?,
      @SortDefault(sort = ["name"]) sort: Sort,
  ): Page<ModResponseDto> {
    logger.debug { "GET /mods - page: $page, size: $size, name: $name" }
    return modService.findModsWithFilter(page, size, name, sort).map { ModResponseDto.fromMod(it) }
  }

  @GetMapping("/{modId}/versions")
  fun getModVersions(
      @PathVariable modId: String,
      @RequestParam @Min(0) page: Int,
      @RequestParam @Min(1) size: Int,
      @SortDefault(sort = ["id"], direction = Sort.Direction.DESC) sort: Sort,
      @AuthenticationPrincipal user: UserDetails?,
  ): Page<VersionResponseDto> {
    val authenticatedUser = requireUser(user)
    logger.info { "GET /mods/$modId/versions - page: $page, size: $size" }
    return versionService
        .getModVersions(
            UserId(authenticatedUser.username), ModId(modId), PageRequest.of(page, size, sort))
        .map { VersionResponseDto.fromVersion(it) }
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  fun uploadMod(
      @Valid @ModelAttribute dto: ModUploadDto,
      @AuthenticationPrincipal user: UserDetails?,
  ): ModResponseDto {
    val authenticatedUser = requireUser(user)
    logger.info {
      "POST /mods - uploading mod: ${dto.name} for user: ${authenticatedUser.username}"
    }
    return modService.uploadMod(UserId(authenticatedUser.username), dto.toModCreateCommand()).let {
      ModResponseDto.fromMod(it)
    }
  }

  @DeleteMapping("/{modId}")
  fun deleteMod(
      @PathVariable modId: ModId,
      @AuthenticationPrincipal user: UserDetails?,
  ) {
    val authenticatedUser = requireUser(user)
    logger.info { "DELETE /mods/$modId for user: ${authenticatedUser.username}" }
    modService.deleteMod(UserId(authenticatedUser.username), modId)
  }

  private fun requireUser(user: UserDetails?): UserDetails =
      user ?: throw ResponseStatusException(UNAUTHORIZED, "Authentication required")
}
