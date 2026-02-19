package git.walhay.modweave.services

import git.walhay.modweave.dto.ModUploadDTO
import git.walhay.modweave.models.Mod
import git.walhay.modweave.models.Version
import git.walhay.modweave.repositories.VersionRepository
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.sql.Date

@Service
@Transactional
class VersionService @Autowired constructor(
    private val versionRepository: VersionRepository
) {

    fun initModVersion(mod: Mod, modUploadDTO: ModUploadDTO): Version {
        val version = Version(modUploadDTO.versionName, "", Date(System.currentTimeMillis()), "", mod)

        return versionRepository.save(version)
    }
}