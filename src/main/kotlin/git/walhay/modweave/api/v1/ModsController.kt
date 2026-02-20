package git.walhay.modweave.api.v1

import git.walhay.modweave.dto.ModUploadDTO
import git.walhay.modweave.dto.VersionUploadDTO
import git.walhay.modweave.models.Version
import git.walhay.modweave.repositories.ModRepository
import git.walhay.modweave.services.ModService
import git.walhay.modweave.services.VersionService
import io.minio.GetPresignedObjectUrlArgs
import io.minio.MinioClient
import io.minio.http.Method
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import kotlin.jvm.optionals.getOrNull

@RestController
@RequestMapping("/api/v1/mods")
class ModsController
@Autowired
constructor(
    private val modRepository: ModRepository,
    private val modService: ModService,
    private val versionService: VersionService,
    private val minioClient: MinioClient
) {

  @GetMapping
  fun getMods(@RequestParam page: Int, @RequestParam pageSize: Int) =
      modRepository.findAll(PageRequest.of(page, pageSize))

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
}
