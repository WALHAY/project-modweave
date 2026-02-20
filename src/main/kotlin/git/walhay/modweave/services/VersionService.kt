package git.walhay.modweave.services

import git.walhay.modweave.dto.ModUploadDTO
import git.walhay.modweave.dto.VersionUploadDTO
import git.walhay.modweave.models.Mod
import git.walhay.modweave.models.Version
import git.walhay.modweave.repositories.VersionRepository
import jakarta.transaction.Transactional
import java.sql.Date
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
    val version = Version(modUploadDTO.versionName, "", Date(System.currentTimeMillis()), mod)
    val savedVersion = versionRepository.save(version)

    fileService.uploadNewFiles(savedVersion, modUploadDTO.files)
    return savedVersion
  }

  fun uploadNewModVersion(mod: Mod, versionUploadDTO: VersionUploadDTO): Version {
    val version =
        Version(
            versionUploadDTO.name, versionUploadDTO.changes, Date(System.currentTimeMillis()), mod)
    return versionRepository.save(version)
  }
}
