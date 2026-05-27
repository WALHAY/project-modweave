package git.walhay.modweave.api.version.http

import git.walhay.modweave.api.file.IFileService
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.version.IVersionService
import git.walhay.modweave.api.version.VersionId
import git.walhay.modweave.api.version.http.dto.VersionResponseDto
import git.walhay.modweave.api.version.http.dto.VersionUploadDto
import jakarta.validation.Valid
import java.util.UUID
import mu.KLogger
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/mods/{modId}/versions")
class VersionController(
    private val versionService: IVersionService,
    private val fileService: IFileService,
) {
  private val logger: KLogger = KotlinLogging.logger {}

  @GetMapping("/{versionId}")
  fun getModVersion(
      @PathVariable modId: String,
      @PathVariable versionId: String,
  ): VersionResponseDto {
    logger.info { "GET /mods/$modId/versions/$versionId" }
    return versionService.getModVersion(VersionId(UUID.fromString(versionId))).let {
      VersionResponseDto.fromVersion(it)
    }
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  fun uploadModVersion(
      @PathVariable modId: ModId,
      @Valid @ModelAttribute dto: VersionUploadDto,
  ): VersionResponseDto {
    logger.info { "POST /mods/$modId/versions - uploading version: ${dto.name}" }
    return versionService.createModVersion(modId, dto.toVersionCreateCommand()).let {
      VersionResponseDto.fromVersion(it)
    }
  }

  @PostMapping("/{versionId}/files")
  fun uploadVersionFiles(
      @PathVariable modId: String,
      @PathVariable versionId: String,
      @RequestParam("files") files: List<MultipartFile>,
  ): VersionResponseDto {
    logger.info { "POST /mods/$modId/versions/$versionId/files - uploading files" }
    val version = versionService.getModVersion(VersionId(UUID.fromString(versionId)))
    fileService.uploadVersionFiles(version, files)
    return VersionResponseDto.fromVersion(versionService.getModVersion(VersionId(UUID.fromString(versionId))))
  }

  @DeleteMapping("/{versionId}")
  fun deleteVersion(
      @PathVariable modId: String,
      @PathVariable versionId: String,
  ) {
    logger.info { "DELETE /mods/$modId/versions/$versionId" }
    versionService.deleteModVersion(VersionId(UUID.fromString(versionId)))
  }
}
