package git.walhay.modweave.api.version

import git.walhay.modweave.api.file.IFileService
import git.walhay.modweave.api.mod.IModService
import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.mod.command.ModCreateCommand
import git.walhay.modweave.api.version.command.VersionCreateCommand
import git.walhay.modweave.api.version.exception.VersionExistsException
import git.walhay.modweave.api.version.exception.VersionNotFoundException
import git.walhay.modweave.api.version.repository.VersionRepository
import jakarta.transaction.Transactional
import mu.KLogger
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
@Transactional
class VersionService(
    private val versionRepository: VersionRepository,
    private val fileService: IFileService,
    private val logger: KLogger = KotlinLogging.logger {}
) : IVersionService {
  @Lazy @Autowired private lateinit var modService: IModService

  @Cacheable("versions", key = "#versionId")
  override fun getModVersion(versionId: VersionId): Version =
      versionRepository.findVersionById(versionId) ?: throw VersionNotFoundException(versionId)

  override fun getModVersions(modId: ModId, pageable: Pageable): Page<Version> {
    return versionRepository.findVersionsByModId(modId, pageable)
  }

  @CachePut("versions", key = "#result.id")
  override fun createModVersion(mod: Mod, command: ModCreateCommand): Version {
    val version = Version(command.versionName, null, mod.id).let { versionRepository.save(it) }
    mod.versions.addLast(version)

    fileService.uploadVersionFiles(version, command.files)
    return versionRepository.save(version)
  }

  @CachePut("versions", key = "#result.id")
  override fun createModVersion(modId: ModId, command: VersionCreateCommand): Version {
    val mod = modService.findModById(modId)

    if (mod.versions.any { it.name == command.name }) {
      throw VersionExistsException(command.name)
    }

    val version =
        command
            .let { (name, changes) -> Version(name, changes, mod.id) }
            .let { versionRepository.save(it) }

    fileService.uploadVersionFiles(version, command.files)
    return versionRepository.save(version)
  }

  @CacheEvict("versions", key = "#versionId")
  override fun deleteModVersion(versionId: VersionId) {
    val version = getModVersion(versionId)

    versionRepository.delete(version.id)
  }
}
