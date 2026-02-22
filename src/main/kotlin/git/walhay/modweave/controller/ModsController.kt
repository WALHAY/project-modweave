package git.walhay.modweave.controller

import git.walhay.modweave.dto.ModUploadDTO
import git.walhay.modweave.dto.VersionUploadDTO
import git.walhay.modweave.model.Mod
import git.walhay.modweave.model.Version
import git.walhay.modweave.repository.ModRepository
import git.walhay.modweave.service.ModService
import git.walhay.modweave.service.VersionService
import io.minio.GetPresignedObjectUrlArgs
import io.minio.MinioClient
import io.minio.http.Method
import kotlin.jvm.optionals.getOrNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.web.SortDefault
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/mods")
class ModsController
@Autowired
constructor(
    private val modRepository: ModRepository,
    private val modService: ModService,
    private val versionService: VersionService,
    private val minioClient: MinioClient
) {

  @GetMapping
  fun getMods(
      @RequestParam page: Int,
      @RequestParam size: Int,
      @RequestParam(required = false) name: String?,
      @SortDefault(sort = ["name"]) sort: Sort
  ): Page<Mod> {
    val pageRequest = PageRequest.of(page, size, sort)
    if (name == null) {
      return modRepository.findAll(pageRequest)
    }
    return modRepository.findAllByNameContainingIgnoreCase(name, pageRequest)
  }

  @PostMapping
  fun uploadMod(@ModelAttribute modUploadForm: ModUploadDTO) {
    SecurityContextHolder.getContext().authentication?.name?.let {
      modService.uploadNewMod(it, modUploadForm)
    }
  }

  @GetMapping("/{modId}")
  fun downloadFile(
      @PathVariable modId: String,
      @RequestParam(required = true) version: String,
      @RequestParam(required = true) filename: String
  ): String {
    val modName = modRepository.findById(modId).get().name
    val fullname = "${modName}/${version}/${filename}"
    return minioClient.getPresignedObjectUrl(
        GetPresignedObjectUrlArgs.builder()
            .bucket("mods")
            .method(Method.GET)
            .`object`(fullname)
            .expiry(60 * 10)
            .build())
  }

  @PostMapping("/{modId}")
  fun uploadVersion(
      @ModelAttribute versionUploadDTO: VersionUploadDTO,
      @PathVariable modId: String
  ): Version {
    val mod = modRepository.findById(modId).getOrNull() ?: throw Exception()

    return versionService.uploadNewModVersion(mod, versionUploadDTO)
  }

  @DeleteMapping("/{modId}")
  fun deleteMod(@PathVariable modId: String) {
    modRepository.deleteById(modId)
  }
}
