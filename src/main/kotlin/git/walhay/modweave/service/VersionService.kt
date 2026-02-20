package git.walhay.modweave.service

import git.walhay.modweave.dto.ModUploadDTO
import git.walhay.modweave.dto.VersionUploadDTO
import git.walhay.modweave.model.Mod
import git.walhay.modweave.model.Version
import git.walhay.modweave.repository.VersionRepository
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
@Transactional
class VersionService
@Autowired
constructor(
    private val versionRepository: VersionRepository,
    private val fileService: FileService,
) {

  fun initModVersion(mod: Mod, modUploadDTO: ModUploadDTO): Version {
    val version = Version(modUploadDTO.versionName, "", mod)
    val savedVersion = versionRepository.save(version)

    fileService.uploadNewFiles(savedVersion, modUploadDTO.files)
    return savedVersion
  }

  fun uploadNewModVersion(mod: Mod, versionUploadDTO: VersionUploadDTO): Version {
    val version =
        Version(
            versionUploadDTO.name, versionUploadDTO.changes, mod)
    return versionRepository.save(version)
  }
}
