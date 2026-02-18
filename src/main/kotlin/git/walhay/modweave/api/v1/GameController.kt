package git.walhay.modweave.api.v1

import git.walhay.modweave.models.Game
import git.walhay.modweave.repositories.GameRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/games")
class GameController @Autowired constructor(private val gameRepository: GameRepository) {

  @GetMapping
  fun getGames(@RequestParam page: Int, @RequestParam pageSize: Int): Page<Game> =
      gameRepository.findAll(PageRequest.of(page, pageSize))

  @PostMapping fun addGame() {}
}
