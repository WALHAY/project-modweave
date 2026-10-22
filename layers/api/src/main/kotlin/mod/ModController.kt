package git.walhay.modweave.api.mod

import git.walhay.modweave.api.common.apiLink
import git.walhay.modweave.api.common.toSortOrders
import git.walhay.modweave.api.security.requireUser
import git.walhay.modweave.api.version.FileResponseDto
import git.walhay.modweave.api.version.VersionResponseDto
import git.walhay.modweave.domain.common.paging.Page
import git.walhay.modweave.domain.common.paging.PageRequest
import git.walhay.modweave.domain.mod.ModId
import git.walhay.modweave.domain.mod.ModService
import git.walhay.modweave.domain.user.UserId
import git.walhay.modweave.domain.version.VersionService
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import mu.KLogger
import mu.KotlinLogging
import org.springframework.data.domain.Sort
import org.springframework.data.web.SortDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/mods")
class ModController(
    private val modService: ModService,
    private val versionService: VersionService,
) {
  private val logger: KLogger = KotlinLogging.logger {}

  @GetMapping("/{modId}")
  fun getMod(
      @PathVariable modId: String,
  ): ModResponseDto {
    logger.info { "GET /mods/$modId" }
    return modService.findModById(ModId(modId)).let { ModResponseDto.fromMod(it) }
  }

  @GetMapping
  fun getMods(
      @RequestParam @Min(0) page: Int,
      @RequestParam @Min(1) size: Int,
      @RequestParam(required = false) name: String?,
      @SortDefault(sort = ["name"]) sort: Sort,
  ): Page<ModResponseDto> {
    logger.debug { "GET /mods - page: $page, size: $size, name: $name" }
    return modService.findModsWithFilter(page, size, name, sort.toSortOrders("name", "id")).map {
      ModResponseDto.fromMod(it)
    }
  }

  @GetMapping("/{modId}/versions")
  fun getModVersions(
      @PathVariable modId: String,
      @RequestParam @Min(0) page: Int,
      @RequestParam @Min(1) size: Int,
      @SortDefault(sort = ["id"], direction = Sort.Direction.DESC) sort: Sort,
      @AuthenticationPrincipal user: UserDetails?,
  ): Page<VersionResponseDto<FileResponseDto>> {
    logger.info { "GET /mods/$modId/versions - page: $page, size: $size" }
    modService.findModById(ModId(modId))
    return versionService
        .getModVersions(
            user?.username?.let { UserId(it) },
            ModId(modId),
            PageRequest(page, size, sort.toSortOrders("id", "name", "uploadDate")))
        .map { VersionResponseDto.fromVersion(it) }
  }

  @PostMapping(version = "1", consumes = ["multipart/form-data"])
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

  @PostMapping(version = "2", consumes = ["multipart/form-data"])
  fun createMod(
      @Valid @ModelAttribute dto: ModUploadDto,
      @AuthenticationPrincipal user: UserDetails?,
  ): ResponseEntity<ModResponseDto> {
    val result = uploadMod(dto, user)
    return ResponseEntity.created(apiLink(ModController::getMod, result.id).link.toUri())
        .body(result)
  }

  @DeleteMapping("/{modId}", version = "1")
  fun deleteMod(
      @PathVariable modId: String,
      @AuthenticationPrincipal user: UserDetails?,
  ) {
    val authenticatedUser = requireUser(user)
    logger.info { "DELETE /mods/$modId for user: ${authenticatedUser.username}" }
    modService.deleteMod(UserId(authenticatedUser.username), ModId(modId))
  }

  @DeleteMapping("/{modId}", version = "2")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun removeMod(@PathVariable modId: String, @AuthenticationPrincipal user: UserDetails?) =
      deleteMod(modId, user)
}
