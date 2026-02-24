package git.walhay.modweave.api.mod

import git.walhay.modweave.api.category.CategoryRepository
import git.walhay.modweave.api.game.GameRepository
import git.walhay.modweave.api.mod.dto.ModDto
import git.walhay.modweave.api.mod.dto.ModUploadDto
import git.walhay.modweave.api.mod.exception.ModNotFoundException
import git.walhay.modweave.api.user.UserRepository
import git.walhay.modweave.api.version.Version
import git.walhay.modweave.api.version.VersionService
import git.walhay.modweave.api.version.dto.VersionUploadDto
import git.walhay.modweave.util.spinalCase
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@Service
@Transactional
class ModService(
    private val modRepository: ModRepository,
    private val userRepository: UserRepository,
    private val categoryRepository: CategoryRepository,
    private val gameRepository: GameRepository,
    private val versionService: VersionService
) {

  fun findModById(id: String): ModDto =
      modRepository
          .findById(id)
          .orElseThrow { ModNotFoundException("Mod with id=$id not found") }
          .toModDto()

  fun findModsWithFilter(page: Int, size: Int, name: String?, sort: Sort): Page<ModDto> {
    val pageRequest = PageRequest.of(page, size, sort)
    if (name == null) {
      return modRepository.findAll(pageRequest).map { it.toModDto() }
    }
    return modRepository.findAllByNameContainingIgnoreCase(name, pageRequest).map { it.toModDto() }
  }

  fun uploadMod(login: String, modUploadForm: ModUploadDto) {
    if (modRepository.existsById(modUploadForm.name.spinalCase())) {
      throw Exception()
    }

    val user = userRepository.findByLoginIgnoreCase(login) ?: throw Exception()
    val game = gameRepository.findById(modUploadForm.game).getOrNull() ?: throw Exception()
    val categories = categoryRepository.findAllByNameIn(modUploadForm.categories)
    val versions: List<Version> = mutableListOf()
    // TODO: add image path
    val mod =
        Mod(modUploadForm.name, modUploadForm.description, user, game, "", categories, versions)
    modRepository.save(mod)
    val initVersion =
        versionService.uploadModVersion(
            mod, VersionUploadDto(modUploadForm.versionName, "", modUploadForm.files))
    mod.versions + initVersion
    modRepository.save(mod)
  }

  fun deleteMod(login: String, modId: String) {
    modRepository.deleteById(modId)
  }
}
