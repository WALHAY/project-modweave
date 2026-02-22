package git.walhay.modweave.service

import git.walhay.modweave.dto.VersionUploadDTO
import git.walhay.modweave.model.Mod
import git.walhay.modweave.model.Version
import git.walhay.modweave.repository.VersionRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
@Transactional
class VersionService(
    private val versionRepository: VersionRepository,
    private val fileService: FileService,
) {

    fun uploadModVersion(mod: Mod, modVersionUploadDTO: VersionUploadDTO): Version {
        val version = Version(modVersionUploadDTO.name, "", mod)
        val savedVersion = versionRepository.save(version)

        fileService.uploadNewFiles(savedVersion, modVersionUploadDTO.files)
        return savedVersion
    }
}
