package git.walhay.modweave.service

import git.walhay.modweave.dto.ModUploadDTO
import git.walhay.modweave.dto.VersionUploadDTO
import git.walhay.modweave.exception.ModNotFoundException
import git.walhay.modweave.model.Mod
import git.walhay.modweave.model.Version
import git.walhay.modweave.repository.CategoryRepository
import git.walhay.modweave.repository.GameRepository
import git.walhay.modweave.repository.ModRepository
import git.walhay.modweave.repository.UserRepository
import git.walhay.modweave.util.spinalCase
import kotlin.jvm.optionals.getOrNull
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
    private val versionService: VersionService
) {

  fun findModById(id: String) =
      modRepository.findById(id).orElseThrow { ModNotFoundException("Mod with id=$id not found") }

  fun findModsWithFilter(page: Int, size: Int, name: String?, sort: Sort): Page<Mod> {
    val pageRequest = PageRequest.of(page, size, sort)
    if (name == null) {
      return modRepository.findAll(pageRequest)
    }
    return modRepository.findAllByNameContainingIgnoreCase(name, pageRequest)
  }

  fun uploadNewMod(login: String, modUploadForm: ModUploadDTO) {
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
            mod, VersionUploadDTO(modUploadForm.versionName, "", modUploadForm.files))
    mod.versions + initVersion
  }

  fun deleteMod(login: String, modId: String) {
    val mod = modRepository.findById(modId).get()

    modRepository.delete(mod)
  }
}
