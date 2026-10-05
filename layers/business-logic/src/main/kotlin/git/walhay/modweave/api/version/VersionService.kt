package git.walhay.modweave.api.version

import git.walhay.modweave.api.common.paging.Page
import git.walhay.modweave.api.common.paging.PageSizePolicy
import git.walhay.modweave.api.file.IFileService
import git.walhay.modweave.api.mod.IModService
import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.mod.command.ModCreateCommand
import git.walhay.modweave.api.security.AccessSecurity
import git.walhay.modweave.api.storage.ISimpleStorageService
import git.walhay.modweave.api.user.UserId
import git.walhay.modweave.api.version.command.VersionCreateCommand
import git.walhay.modweave.api.version.exception.VersionExistsException
import git.walhay.modweave.api.version.exception.VersionNotFoundException
import git.walhay.modweave.api.version.repository.VersionRepository
import jakarta.transaction.Transactional
import mu.KLogger
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
@Transactional(rollbackOn = [Exception::class])
class VersionService(
    private val versionRepository: VersionRepository,
    private val fileService: IFileService,
    private val accessSecurity: AccessSecurity,
    private val pageSizePolicy: PageSizePolicy,
    private val storageService: ISimpleStorageService,
    private val logger: KLogger = KotlinLogging.logger {},
) : IVersionService {
  @Lazy @Autowired private lateinit var modService: IModService

  @Cacheable("versions", key = "#p0")
  @PreAuthorize("@accessSecurity.canReadVersion(#p0)")
  override fun getModVersion(versionId: VersionId): Version =
      versionRepository.findVersionById(versionId) ?: throw VersionNotFoundException(versionId)

  override fun getModVersions(
      userId: UserId?,
      modId: ModId,
      pageable: Pageable,
  ): Page<Version> {
    val boundedPage =
        PageRequest.of(
            pageable.pageNumber, pageSizePolicy.normalize(pageable.pageSize), pageable.sort)
    if (userId != null && accessSecurity.isModOwnerOrAdmin(userId.value, modId.value)) {
      return versionRepository.findVersionsByModId(modId, boundedPage)
    }
    return versionRepository.findVersionsByModIdAndStatus(
        modId, VersionStatus.APPROVED, boundedPage)
  }

  @CacheEvict("versions", allEntries = true)
  @PreAuthorize("isAuthenticated()")
  override fun createModVersion(
      mod: Mod,
      command: ModCreateCommand,
  ): Version {
    if (!accessSecurity.canManageMod(mod.id.value))
        throw org.springframework.security.access.AccessDeniedException("Mod ownership required")
    val version = Version(command.versionName, null, mod.id).let { versionRepository.save(it) }
    mod.versions.addLast(version)

    fileService.uploadVersionFiles(version, command.files)
    return versionRepository.save(version)
  }

  @CacheEvict(value = ["versions", "mods", "games", "collections", "users"], allEntries = true)
  @PreAuthorize("@accessSecurity.isAdmin()")
  override fun changeVersionStatus(
      userId: UserId,
      modId: ModId,
      versionId: VersionId,
      status: VersionStatus
  ): Version {
    val version = getModVersion(versionId)
    if (version.modId != modId) throw VersionNotFoundException(versionId)
    require(status != VersionStatus.APPROVED || version.files.isNotEmpty()) {
      "Version must contain at least one file"
    }
    version.status = status

    return versionRepository.save(version)
  }

  @CacheEvict(value = ["versions", "mods", "games", "collections", "users"], allEntries = true)
  @PreAuthorize("@accessSecurity.canManageMod(#p0)")
  override fun createModVersion(
      modId: ModId,
      command: VersionCreateCommand,
  ): Version {
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

  @CacheEvict(value = ["versions", "mods", "games", "collections", "users"], allEntries = true)
  @PreAuthorize("@accessSecurity.canManageMod(#p0)")
  override fun deleteModVersion(modId: ModId, versionId: VersionId) {
    val version = getModVersion(versionId)

    if (version.modId != modId) throw VersionNotFoundException(versionId)
    versionRepository.delete(version.id)
    version.files.forEach { storageService.removeVersionFile(it.filePath) }
  }
}
