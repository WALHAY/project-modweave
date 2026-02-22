package git.walhay.modweave.controller

import git.walhay.modweave.dto.ModUploadDTO
import git.walhay.modweave.model.Mod
import git.walhay.modweave.service.ModService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort
import org.springframework.data.web.SortDefault
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/mods")
class ModsController
(
    private val modService: ModService,
) {

    @GetMapping("/{modId}")
    fun getMod(@PathVariable modId: String) = modService.findModById(modId)

  @GetMapping
  fun getMods(
      @RequestParam page: Int,
      @RequestParam size: Int,
      @RequestParam(required = false) name: String?,
      @SortDefault(sort = ["name"]) sort: Sort
  ): Page<Mod> = modService.findModsWithFilter(page, size, name, sort)

  @PostMapping
  fun uploadMod(@Valid @ModelAttribute modUploadForm: ModUploadDTO) {
      SecurityContextHolder.getContext().authentication?.name?.let {
          modService.uploadNewMod(it, modUploadForm)
      }
  }

  @DeleteMapping("/{modId}")
  fun deleteMod(@PathVariable modId: String) {
      SecurityContextHolder.getContext().authentication?.name?.let {
          modService.deleteMod(it, modId)
      }
  }
}
