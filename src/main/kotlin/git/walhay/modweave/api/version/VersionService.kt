package git.walhay.modweave.api.version

import git.walhay.modweave.api.file.FileService
import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.api.mod.ModRepository
import git.walhay.modweave.api.mod.exception.ModNotFoundException
import git.walhay.modweave.api.version.dto.VersionDto
import git.walhay.modweave.api.version.dto.VersionUploadDto
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
@Transactional
class VersionService(
    private val versionRepository: VersionRepository,
    private val fileService: FileService,
    private val modRepository: ModRepository,
) {

  fun uploadModVersion(mod: Mod, modVersionUploadDTO: VersionUploadDto): VersionDto {
    val version = Version(modVersionUploadDTO.name, "", mod)
    val savedVersion = versionRepository.save(version)

    fileService.uploadNewFiles(savedVersion, modVersionUploadDTO.files)
    return savedVersion.toVersionDto()
  }

  fun uploadModVersion(modId: String, modVersionUploadDTO: VersionUploadDto): VersionDto {
    val mod =
        modRepository.findById(modId).orElseThrow {
          throw ModNotFoundException("Mod with id=$modId not found")
        }
    return uploadModVersion(mod, modVersionUploadDTO)
  }
}
