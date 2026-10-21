package git.walhay.modweave.domain.version

import git.walhay.modweave.domain.common.paging.Page
import git.walhay.modweave.domain.common.paging.PageSizePolicy
import git.walhay.modweave.domain.file.FileService
import git.walhay.modweave.domain.mod.Mod
import git.walhay.modweave.domain.mod.ModCreateCommand
import git.walhay.modweave.domain.mod.ModId
import git.walhay.modweave.domain.mod.ModNotFoundException
import git.walhay.modweave.domain.mod.ModRepository
import git.walhay.modweave.domain.security.AccessSecurity
import git.walhay.modweave.domain.storage.ISimpleStorageService
import git.walhay.modweave.domain.user.UserId
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(rollbackFor = [Exception::class])
class VersionService(
    private val versionRepository: VersionRepository,
    private val modRepository: ModRepository,
    private val fileService: FileService,
    private val accessSecurity: AccessSecurity,
    private val pageSizePolicy: PageSizePolicy,
    private val storageService: ISimpleStorageService,
) {
  @Cacheable("versions", key = "#p0")
  @PreAuthorize("@accessSecurity.canReadVersion(#p0)")
  fun getModVersion(versionId: VersionId): Version =
      versionRepository.findVersionById(versionId) ?: throw VersionNotFoundException(versionId)

  fun getModVersions(
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
  fun createModVersion(
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
  fun changeVersionStatus(
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
  fun createModVersion(
      modId: ModId,
      command: VersionCreateCommand,
  ): Version {
    val mod = modRepository.findById(modId) ?: throw ModNotFoundException(modId)

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
  fun deleteModVersion(modId: ModId, versionId: VersionId) {
    val version = getModVersion(versionId)

    if (version.modId != modId) throw VersionNotFoundException(versionId)
    versionRepository.delete(version.id)
    version.files.forEach { storageService.removeVersionFile(it.filePath) }
  }
}
