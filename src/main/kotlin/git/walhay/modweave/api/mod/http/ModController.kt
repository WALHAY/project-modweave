package git.walhay.modweave.api.mod.http

import git.walhay.modweave.api.mod.IModService
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.mod.http.dto.ModResponseDto
import git.walhay.modweave.api.mod.http.dto.ModUploadDto
import git.walhay.modweave.api.mod.http.dto.fromMod
import git.walhay.modweave.api.user.UserId
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort
import org.springframework.data.web.SortDefault
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/mods")
class ModController(private val modService: IModService) {

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
