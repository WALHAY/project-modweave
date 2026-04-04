package git.walhay.modweave.api.mod

import git.walhay.modweave.api.category.repository.CategoryRepository
import git.walhay.modweave.api.game.IGameService
import git.walhay.modweave.api.mod.command.ModCreateCommand
import git.walhay.modweave.api.mod.exception.ModExistsException
import git.walhay.modweave.api.mod.exception.ModNotFoundException
import git.walhay.modweave.api.mod.repository.ModRepository
import git.walhay.modweave.api.storage.ISimpleStorageService
import git.walhay.modweave.api.user.IUserService
import git.walhay.modweave.api.user.UserId
import git.walhay.modweave.api.version.IVersionService
import mu.KLogger
import mu.KotlinLogging
import org.apache.commons.io.FilenameUtils
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ModService(
    private val modRepository: ModRepository,
    private val userService: IUserService,
    private val categoryRepository: CategoryRepository,
    private val gamerService: IGameService,
    private val versionService: IVersionService,
    private val simpleStorageService: ISimpleStorageService,
    private val logger: KLogger = KotlinLogging.logger {}
) : IModService {

  override fun findModById(modId: ModId): Mod =
      modRepository.findById(modId) ?: throw ModNotFoundException(modId)

  override fun findModsWithFilter(page: Int, size: Int, name: String?, sort: Sort): Page<Mod> {
    val pageRequest = PageRequest.of(page, size, sort)
    if (name == null) {
      return modRepository.findAll(pageRequest)
    }
    return modRepository.findAll(name, pageRequest)
  }

  override fun uploadMod(userId: UserId, command: ModCreateCommand): Mod {
    if (modRepository.existsById(command.id)) {
      throw ModExistsException(command.id)
    }

    val user = userService.findUserByUsername(userId)
    val game = gamerService.findGameById(command.gameId)
    val categories = categoryRepository.findAllByNameIn(command.categories)

    val imagePath =
        "${command.name}/logo.${FilenameUtils.getExtension(command.image.originalFilename)}"

    val mod =
        command
            .let { (id, name, description, image) ->
              Mod(
                  id,
                  name,
                  description,
                  simpleStorageService.uploadImage(imagePath, image),
                  user.username,
                  game.id,
                  categories.map { it.name }.toSet())
            }
            .let { modRepository.save(it) }

    versionService.uploadModVersion(mod, command)
    return modRepository.save(mod)
  }

  override fun deleteMod(userId: UserId, modId: ModId) {
    modRepository.deleteById(modId)
  }
}
