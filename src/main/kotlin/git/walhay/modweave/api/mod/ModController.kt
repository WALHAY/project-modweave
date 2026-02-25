package git.walhay.modweave.api.mod

import git.walhay.modweave.api.mod.dto.ModUploadDto
import git.walhay.modweave.api.user.UserService
import jakarta.validation.Valid
import org.springframework.data.domain.Sort
import org.springframework.data.web.SortDefault
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/mods")
class ModController(private val modService: ModService, private val userService: UserService) {

  @GetMapping("/{modId}") fun getMod(@PathVariable modId: String) = modService.findModById(modId)

  @GetMapping
  fun getMods(
      @RequestParam page: Int,
      @RequestParam size: Int,
      @RequestParam(required = false) name: String?,
      @SortDefault(sort = ["name"]) sort: Sort
  ) = modService.findModsWithFilter(page, size, name, sort)

  @PostMapping
  fun uploadMod(@Valid @ModelAttribute modUploadForm: ModUploadDto) {
    SecurityContextHolder.getContext().authentication?.name?.let {
      modService.uploadMod(it, modUploadForm)
    }
  }

  @DeleteMapping("/{modId}")
  fun deleteMod(@PathVariable modId: String) {
    SecurityContextHolder.getContext().authentication?.name?.let { modService.deleteMod(it, modId) }
  }
}
