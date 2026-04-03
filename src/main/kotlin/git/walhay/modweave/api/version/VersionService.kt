package git.walhay.modweave.api.version

import git.walhay.modweave.api.file.IFileService
import git.walhay.modweave.api.mod.IModService
import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.version.dto.VersionUploadDto
import git.walhay.modweave.api.version.exception.VersionNotFoundException
import git.walhay.modweave.api.version.repository.VersionRepository
import jakarta.transaction.Transactional
import mu.KLogger
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

@Service
@Transactional
class VersionService(
    private val versionRepository: VersionRepository,
    private val fileService: IFileService,
    private val logger: KLogger = KotlinLogging.logger {}
) : IVersionService {
  @Lazy @Autowired private lateinit var modService: IModService

  override fun uploadModVersion(mod: Mod, versionUploadDTO: VersionUploadDto): Version {
    val version = versionRepository.save(Version(versionUploadDTO.name, null, mod.id))
    mod.versions.addLast(version)

    fileService.uploadVersionFiles(version, versionUploadDTO.files)
    return versionRepository.save(version)
  }

  override fun uploadModVersion(modId: ModId, versionUploadDTO: VersionUploadDto): Version =
      uploadModVersion(modService.findModById(modId), versionUploadDTO)

  override fun deleteModVersion(modId: ModId, versionId: VersionId) {
    val mod = modService.findModById(modId)

    mod.versions.find { it.id == versionId }?.let { versionRepository.delete(it.id) }
        ?: throw VersionNotFoundException(versionId)
  }
}
