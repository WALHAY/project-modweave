package git.walhay.modweave.controllers

import git.walhay.modweave.games.GameRepository
import git.walhay.modweave.mods.Mod
import git.walhay.modweave.mods.ModRepository
import git.walhay.modweave.user.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class ApiController
@Autowired
constructor(
    private val userRepository: UserRepository,
    private val modsRepository: ModRepository,
    private val gamesRepository: GameRepository
) {

  @GetMapping("/users") fun getUsers(): Long = userRepository.count()

  @GetMapping("/mods")
  fun getMods(@RequestParam(value = "game", required = false) game: String?): List<Mod> {
    if (game != null) {
      return modsRepository.findByGame(gamesRepository.findByName(game)).toList()
    }
    return modsRepository.findAll(PageRequest.ofSize(10))
  }

  @GetMapping("/games") fun getGames() = gamesRepository.findAll()
}
