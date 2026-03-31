package git.walhay.modweave.api.mod

import git.walhay.modweave.api.category.CategoryName
import git.walhay.modweave.api.category.repository.CategoryRepository
import git.walhay.modweave.api.game.GameId
import git.walhay.modweave.api.game.IGameService
import git.walhay.modweave.api.mod.dto.ModUploadDto
import git.walhay.modweave.api.mod.exception.ModExistsException
import git.walhay.modweave.api.mod.exception.ModNotFoundException
import git.walhay.modweave.api.mod.repository.ModRepository
import git.walhay.modweave.api.storage.ISimpleStorageService
import git.walhay.modweave.api.user.IUserService
import git.walhay.modweave.api.user.UserId
import git.walhay.modweave.api.version.IVersionService
import git.walhay.modweave.api.version.dto.VersionUploadDto
import git.walhay.modweave.util.spinalCase
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

  override fun findModById(id: String): Mod =
      modRepository.findById(id) ?: throw ModNotFoundException("Mod with id=$id not found")

  override fun findModsWithFilter(page: Int, size: Int, name: String?, sort: Sort): Page<Mod> {
    val pageRequest = PageRequest.of(page, size, sort)
    if (name == null) {
      return modRepository.findAll(pageRequest)
    }
    return modRepository.findAll(name, pageRequest)
  }

  override fun uploadMod(login: String, dto: ModUploadDto): Mod {
    if (modRepository.existsById(dto.name.spinalCase())) {
      throw ModExistsException(
          "Mod with id=${dto.name.spinalCase()} or name=${dto.name} already exists")
    }

    val user = userService.findUserByLogin(login)
    val game = gamerService.findGameById(dto.game)
    val categories = categoryRepository.findAllByNameIn(dto.categories)

    val imagePath = "${dto.name}/logo.${FilenameUtils.getExtension(dto.image.originalFilename)}"

    val mod =
        modRepository.save(
            Mod(
                dto.name,
                dto.description,
                UserId(user.id),
                GameId(game.id),
                simpleStorageService.uploadImage(imagePath, dto.image),
                categories.map { CategoryName(it.name) }.toSet()))
    versionService.uploadModVersion(mod, VersionUploadDto(dto.versionName, null, dto.files))
    return modRepository.save(mod)
  }

  override fun deleteMod(login: String, modId: String) {
    modRepository.deleteById(modId)
  }
}
