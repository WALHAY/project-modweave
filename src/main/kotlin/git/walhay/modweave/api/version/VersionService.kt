package git.walhay.modweave.api.version

import git.walhay.modweave.api.file.IFileService
import git.walhay.modweave.api.mod.IModService
import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.mod.command.ModCreateCommand
import git.walhay.modweave.api.version.command.VersionCreateCommand
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

  override fun uploadModVersion(mod: Mod, command: ModCreateCommand): Version {
    val version = versionRepository.save(Version(command.name, null, mod.id))
    mod.versions.addLast(version)

    fileService.uploadVersionFiles(version, command.files)
    return versionRepository.save(version)
  }

  override fun uploadModVersion(modId: ModId, command: VersionCreateCommand): Version {
    val mod = modService.findModById(modId)

    val version =
        command
            .let { (name, changes) -> Version(name, changes, mod.id) }
            .also { versionRepository.save(it) }

    fileService.uploadVersionFiles(version, command.files)
    return versionRepository.save(version)
  }

  override fun deleteModVersion(modId: ModId, versionId: VersionId) {
    val mod = modService.findModById(modId)

    mod.versions.find { it.id == versionId }?.let { versionRepository.delete(it.id) }
        ?: throw VersionNotFoundException(versionId)
  }
}
