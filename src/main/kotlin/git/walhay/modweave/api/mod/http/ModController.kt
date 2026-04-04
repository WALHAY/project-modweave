package git.walhay.modweave.api.mod.http

import git.walhay.modweave.api.mod.IModService
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.mod.exception.ModCreationFailedException
import git.walhay.modweave.api.mod.http.dto.ModResponseDto
import git.walhay.modweave.api.mod.http.dto.ModUploadDto
import git.walhay.modweave.api.mod.toModResponseDto
import git.walhay.modweave.api.user.UserId
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort
import org.springframework.data.web.SortDefault
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/mods")
class ModController(private val modService: IModService) {

  @GetMapping("/{modId}")
  fun getMod(@PathVariable modId: ModId): ModResponseDto =
      modService.findModById(modId).toModResponseDto()

  @GetMapping
  fun getMods(
      @RequestParam page: Int,
      @RequestParam size: Int,
      @RequestParam(required = false) name: String?,
      @SortDefault(sort = ["name"]) sort: Sort
  ): Page<ModResponseDto> =
      modService.findModsWithFilter(page, size, name, sort).map { it.toModResponseDto() }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  fun uploadMod(@Valid @ModelAttribute dto: ModUploadDto): ModResponseDto =
      SecurityContextHolder.getContext()
          .authentication
          ?.name
          ?.let { modService.uploadMod(UserId(it), dto.toModCreateCommand()) }
          ?.toModResponseDto() ?: throw ModCreationFailedException()

  @DeleteMapping("/{modId}")
  fun deleteMod(@PathVariable modId: ModId) {
    SecurityContextHolder.getContext().authentication?.name?.let {
      modService.deleteMod(UserId(it), modId)
    }
  }
}
