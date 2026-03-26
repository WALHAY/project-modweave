package git.walhay.modweave.api.mod

import git.walhay.modweave.api.mod.dto.ModDto
import git.walhay.modweave.api.mod.dto.ModUploadDto
import git.walhay.modweave.api.mod.exception.ModCreationFailedException
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort
import org.springframework.data.web.SortDefault
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/mods")
class ModController(private val modService: ModService) {

  @GetMapping("/{modId}") fun getMod(@PathVariable modId: String) = modService.findModById(modId)

  @GetMapping
  fun getMods(
      @RequestParam page: Int,
      @RequestParam size: Int,
      @RequestParam(required = false) name: String?,
      @SortDefault(sort = ["name"]) sort: Sort
  ): Page<ModDto> = modService.findModsWithFilter(page, size, name, sort).map { it.toModDto() }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  fun uploadMod(@Valid @ModelAttribute modUploadForm: ModUploadDto): ModDto =
      SecurityContextHolder.getContext()
          .authentication
          ?.name
          ?.let { modService.uploadMod(it, modUploadForm) }
          ?.toModDto() ?: throw ModCreationFailedException("Failed to create mod")

  @DeleteMapping("/{modId}")
  fun deleteMod(@PathVariable modId: String): Unit {
    SecurityContextHolder.getContext().authentication?.name?.let { modService.deleteMod(it, modId) }
  }
}
