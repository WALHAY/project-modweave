package git.walhay.modweave.api.mod

import git.walhay.modweave.api.category.exception.CategoryNotFoundException
import git.walhay.modweave.api.category.repository.CategoryRepository
import git.walhay.modweave.api.collection.CollectionId
import git.walhay.modweave.api.common.paging.Page
import git.walhay.modweave.api.common.paging.PageSizePolicy
import git.walhay.modweave.api.game.IGameService
import git.walhay.modweave.api.mod.command.ModCreateCommand
import git.walhay.modweave.api.mod.exception.ModExistsException
import git.walhay.modweave.api.mod.exception.ModNotFoundException
import git.walhay.modweave.api.mod.repository.ModRepository
import git.walhay.modweave.api.security.AccessSecurity
import git.walhay.modweave.api.storage.ISimpleStorageService
import git.walhay.modweave.api.user.IUserService
import git.walhay.modweave.api.user.UserId
import git.walhay.modweave.api.version.IVersionService
import git.walhay.modweave.api.version.VersionStatus
import java.util.UUID
import mu.KLogger
import mu.KotlinLogging
import org.apache.commons.io.FilenameUtils
import org.springframework.cache.annotation.CacheEvict
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(rollbackFor = [Exception::class])
class ModService(
    private val modRepository: ModRepository,
    private val accessSecurity: AccessSecurity,
    private val userService: IUserService,
    private val categoryRepository: CategoryRepository,
    private val gamerService: IGameService,
    private val versionService: IVersionService,
    private val simpleStorageService: ISimpleStorageService,
    private val pageSizePolicy: PageSizePolicy,
    private val logger: KLogger = KotlinLogging.logger {},
) : IModService {
  override fun findModById(modId: ModId): Mod =
      modRepository.findById(modId, VersionStatus.APPROVED)
          ?: if (accessSecurity.canManageMod(modId.value)) {
            modRepository.findById(modId)
          } else {
            null
          }
          ?: throw ModNotFoundException(modId)

  override fun findModsWithFilter(
      page: Int,
      size: Int,
      name: String?,
      sort: Sort,
  ): Page<Mod> {
    val pageRequest = PageRequest.of(page, pageSizePolicy.normalize(size), sort)
    val visibleStatus = if (accessSecurity.isAdmin()) null else VersionStatus.APPROVED
    if (name == null) {
      return modRepository.findAll(pageRequest, visibleStatus)
    }
    return modRepository.findAll(name, pageRequest, visibleStatus)
  }

  override fun findModsOfUser(
      id: UserId,
      page: Int,
      size: Int,
      sort: Sort,
  ): Page<Mod> =
      modRepository.findAllByUser(
          id,
          PageRequest.of(page, pageSizePolicy.normalize(size), sort),
          if (accessSecurity.isSelfOrAdmin(id.value)) null else VersionStatus.APPROVED)

  override fun findModsInCollection(
      id: CollectionId,
      page: Int,
      size: Int,
      sort: Sort,
  ): Page<Mod> =
      modRepository.findModsInCollection(
          id, PageRequest.of(page, pageSizePolicy.normalize(size), sort), VersionStatus.APPROVED)

  @CacheEvict(value = ["mods", "games", "users"], allEntries = true)
  @PreAuthorize("@accessSecurity.isSelf(#p0)")
  override fun uploadMod(
      userId: UserId,
      command: ModCreateCommand,
  ): Mod {
    if (modRepository.existsById(command.id)) {
      throw ModExistsException(command.id)
    }

    val user = userService.findUserByUsername(userId)
    val game = gamerService.findGameById(command.gameId)
    val categories = categoryRepository.findAllByNameIn(command.categories)
    command.categories
        .firstOrNull { requested -> categories.none { it.name == requested } }
        ?.let { throw CategoryNotFoundException(it) }

    val imagePath =
        "mods/${command.id.value}/${UUID.randomUUID()}/logo.${FilenameUtils.getExtension(command.image.originalFilename)}"

    val storedImage = simpleStorageService.uploadImage(imagePath, command.image)
    val mod =
        command
            .let { (id, name, description, image) ->
              Mod(
                  id,
                  name,
                  description,
                  storedImage,
                  user.username,
                  game.id,
                  categories.map { it.name }.toSet(),
              )
            }
            .let { modRepository.save(it) }

    versionService.createModVersion(mod, command)
    return modRepository.save(mod)
  }

  @CacheEvict(
      value = ["mods", "versions", "games", "collections", "users", "comments"], allEntries = true)
  @PreAuthorize("@accessSecurity.isModOwnerOrAdmin(#p0, #p1)")
  override fun deleteMod(
      userId: UserId,
      modId: ModId,
  ) {
    val mod = modRepository.findById(modId) ?: throw ModNotFoundException(modId)
    modRepository.deleteById(modId)
    mod.versions
        .flatMap { it.files }
        .forEach { simpleStorageService.removeVersionFile(it.filePath) }
    simpleStorageService.removeImage(mod.imagePath)
  }
}
