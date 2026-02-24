package git.walhay.modweave.service

import git.walhay.modweave.dto.VersionDto
import git.walhay.modweave.dto.VersionUploadDTO
import git.walhay.modweave.exception.ModNotFoundException
import git.walhay.modweave.model.Mod
import git.walhay.modweave.model.Version
import git.walhay.modweave.model.toVersionDto
import git.walhay.modweave.repository.ModRepository
import git.walhay.modweave.repository.VersionRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
@Transactional
class VersionService(
    private val versionRepository: VersionRepository,
    private val fileService: FileService,
    private val modRepository: ModRepository,
) {

  fun uploadModVersion(mod: Mod, modVersionUploadDTO: VersionUploadDTO): VersionDto {
    val version = Version(modVersionUploadDTO.name, "", mod)
    val savedVersion = versionRepository.save(version)

    fileService.uploadNewFiles(savedVersion, modVersionUploadDTO.files)
    return savedVersion.toVersionDto()
  }

  fun uploadModVersion(modId: String, modVersionUploadDTO: VersionUploadDTO): VersionDto {
    val mod =
        modRepository.findById(modId).orElseThrow {
          throw ModNotFoundException("Mod with id=$modId not found")
        }
    return uploadModVersion(mod, modVersionUploadDTO)
  }
}
