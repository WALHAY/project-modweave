package git.walhay.modweave.services

import git.walhay.modweave.dto.ModUploadDTO
import git.walhay.modweave.models.Mod
import git.walhay.modweave.models.Version
import git.walhay.modweave.repositories.CategoryRepository
import git.walhay.modweave.repositories.GameRepository
import git.walhay.modweave.repositories.ModRepository
import git.walhay.modweave.repositories.UserRepository
import git.walhay.modweave.utils.spinalCase
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@Service
@Transactional
class ModService
@Autowired
constructor(
    private val modRepository: ModRepository,
    private val userRepository: UserRepository,
    private val categoryRepository: CategoryRepository,
    private val gameRepository: GameRepository,
    private val versionService: VersionService
) {

  fun uploadNewMod(login: String, modUploadForm: ModUploadDTO) {
      if(modRepository.existsById(modUploadForm.name.spinalCase())) {
          throw Exception()
      }

    val user = userRepository.findByLoginIgnoreCase(login) ?: throw Exception()
    val game = gameRepository.findById(modUploadForm.game).getOrNull() ?: throw Exception()
    val categories = categoryRepository.findAllByNameIn(modUploadForm.categories)

    val versions: List<Version> = mutableListOf()
    val mod =
        Mod(modUploadForm.name, modUploadForm.description, user, game, categories, versions)
    modRepository.save(mod)
    val initVersion = versionService.initModVersion(mod, modUploadForm)
    mod.versions + initVersion
  }
}
