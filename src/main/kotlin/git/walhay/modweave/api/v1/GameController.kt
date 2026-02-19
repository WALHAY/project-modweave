package git.walhay.modweave.api.v1

import git.walhay.modweave.dto.AddGameDTO
import git.walhay.modweave.models.Game
import git.walhay.modweave.repositories.GameRepository
import git.walhay.modweave.services.GameService
import kotlin.jvm.optionals.getOrNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/games")
class GameController
@Autowired
constructor(private val gameRepository: GameRepository, private val gameService: GameService) {

  @GetMapping
  fun getGames(@RequestParam page: Int, @RequestParam pageSize: Int): Page<Game> =
      gameRepository.findAll(PageRequest.of(page, pageSize))

  @GetMapping("/{gameId}")
  fun getGame(@PathVariable gameId: Long): Game? = gameRepository.findById(gameId).getOrNull()

  @PostMapping
  fun addGame(@ModelAttribute addGame: AddGameDTO): Game = gameService.addNewGame(addGame)
}
