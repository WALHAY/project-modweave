package git.walhay.modweave.api.mod.http

import git.walhay.modweave.api.mod.IModService
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.mod.http.dto.ModResponseDto
import git.walhay.modweave.api.mod.http.dto.ModUploadDto
import git.walhay.modweave.api.mod.http.dto.fromMod
import git.walhay.modweave.api.user.UserId
import git.walhay.modweave.api.version.IVersionService
import git.walhay.modweave.api.version.http.dto.VersionResponseDto
import git.walhay.modweave.api.version.http.dto.fromVersion
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.web.SortDefault
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/mods")
class ModController(
    private val modService: IModService,
    private val versionService: IVersionService
) {

  @GetMapping("/{modId}")
  fun getMod(@PathVariable modId: ModId): ModResponseDto =
      modService.findModById(modId).let { ModResponseDto.fromMod(it) }

  @GetMapping
  fun getMods(
      @RequestParam page: Int,
      @RequestParam size: Int,
      @RequestParam(required = false) name: String?,
      @SortDefault(sort = ["name"]) sort: Sort
  ): Page<ModResponseDto> =
      modService.findModsWithFilter(page, size, name, sort).map { ModResponseDto.fromMod(it) }

  @GetMapping("/{modId}/versions")
  fun getModVersions(
      @PathVariable modId: String,
      @RequestParam @Min(0) page: Int,
      @RequestParam @Min(1) size: Int,
      @SortDefault(sort = ["name,desc"]) sort: Sort
  ): Page<VersionResponseDto> =
      versionService.getModVersions(ModId(modId), PageRequest.of(page, size, sort)).map {
        VersionResponseDto.fromVersion(it)
      }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  fun uploadMod(
      @Valid @ModelAttribute dto: ModUploadDto,
      @AuthenticationPrincipal user: UserDetails
  ): ModResponseDto =
      modService.uploadMod(UserId(user.username), dto.toModCreateCommand()).let {
        ModResponseDto.fromMod(it)
      }

  @DeleteMapping("/{modId}")
  fun deleteMod(@PathVariable modId: ModId, @AuthenticationPrincipal user: UserDetails) =
      modService.deleteMod(UserId(user.username), modId)
}
