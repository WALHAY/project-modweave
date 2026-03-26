package git.walhay.modweave.api.mod

import git.walhay.modweave.api.category.repository.CategoryRepository
import git.walhay.modweave.api.game.exception.GameNotFoundException
import git.walhay.modweave.api.game.repository.GameRepository
import git.walhay.modweave.api.mod.dto.ModUploadDto
import git.walhay.modweave.api.mod.exception.ModExistsException
import git.walhay.modweave.api.mod.exception.ModNotFoundException
import git.walhay.modweave.api.mod.repository.ModRepository
import git.walhay.modweave.api.service.SimpleStorageService
import git.walhay.modweave.api.user.exception.UserNotFoundException
import git.walhay.modweave.api.user.repository.UserRepository
import git.walhay.modweave.api.version.VersionService
import git.walhay.modweave.util.spinalCase
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
    private val userRepository: UserRepository,
    private val categoryRepository: CategoryRepository,
    private val gameRepository: GameRepository,
    private val versionService: VersionService,
    private val simpleStorageService: SimpleStorageService
) {

  fun findModById(id: String): Mod =
      modRepository.findById(id) ?: throw ModNotFoundException("Mod with id=$id not found")

  fun findModsWithFilter(page: Int, size: Int, name: String?, sort: Sort): Page<Mod> {
    val pageRequest = PageRequest.of(page, size, sort)
    if (name == null) {
      return modRepository.findAll(pageRequest)
    }
    return modRepository.findAll(name, pageRequest)
  }

  fun uploadMod(login: String, dto: ModUploadDto): Mod {
    if (modRepository.existsById(dto.name.spinalCase())) {
      throw ModExistsException(
          "Mod with id=${dto.name.spinalCase()} or name=${dto.name} already exists")
    }

    val user =
        userRepository.findByLogin(login)
            ?: throw UserNotFoundException("User with login=${login} not found")
    val game =
        gameRepository.findById(dto.game)
            ?: throw GameNotFoundException("Game with id=${dto.game} not found")
    val categories = categoryRepository.findAllByNameIn(dto.categories)

    val imagePath = "${dto.name}/logo.${FilenameUtils.getExtension(dto.image.originalFilename)}"

    val mod =
        Mod(
            dto.name,
            dto.description,
            user,
            game,
            simpleStorageService.uploadImage(imagePath, dto.image),
            categories)
    versionService.uploadModVersionTransient(mod, dto.versionName, dto.files)
    return modRepository.save(mod)
  }

  fun deleteMod(login: String, modId: String) {
    modRepository.deleteById(modId)
  }
}
