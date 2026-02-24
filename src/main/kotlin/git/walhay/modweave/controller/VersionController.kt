package git.walhay.modweave.controller

import git.walhay.modweave.dto.VersionDto
import git.walhay.modweave.dto.VersionUploadDTO
import git.walhay.modweave.service.ModService
import git.walhay.modweave.service.VersionService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/mods/{modId}")
class VersionController(
    private val versionService: VersionService,
    private val modService: ModService
) {

  @PostMapping
  fun uploadModVersion(
      @PathVariable modId: String,
      @Valid @ModelAttribute versionUploadDTO: VersionUploadDTO
  ): ResponseEntity<VersionDto> {
    val version = versionService.uploadModVersion(modId, versionUploadDTO)
    return ResponseEntity.status(HttpStatus.CREATED).body(version)
  }

  @DeleteMapping("/{versionName}")
  fun deleteVersion(@PathVariable modId: String, @PathVariable versionName: String) {}
}
