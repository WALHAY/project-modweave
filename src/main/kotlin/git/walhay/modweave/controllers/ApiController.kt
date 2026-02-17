package git.walhay.modweave.controllers

import git.walhay.modweave.games.GameRepository
import git.walhay.modweave.mods.ModRepository
import git.walhay.modweave.user.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ApiController
@Autowired
constructor(
    private val userRepository: UserRepository,
    private val modsRepository: ModRepository,
    private val gamesRepository: GameRepository
) {
  @GetMapping("/games") fun getGames() = gamesRepository.findAll()
}
