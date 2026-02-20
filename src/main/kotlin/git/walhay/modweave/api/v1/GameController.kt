package git.walhay.modweave.api.v1

import git.walhay.modweave.dto.AddGameDTO
import git.walhay.modweave.models.Game
import git.walhay.modweave.repositories.GameRepository
import git.walhay.modweave.services.GameService
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.*
import kotlin.jvm.optionals.getOrNull

@RestController
@RequestMapping("/api/v1/games")
class GameController
@Autowired
constructor(private val gameRepository: GameRepository, private val gameService: GameService) {

  @GetMapping
  fun getGames(@RequestParam page: Int, @RequestParam pageSize: Int): Page<Game> =
      gameRepository.findAll(PageRequest.of(page, pageSize))

  @GetMapping("/{gameId}")
  fun getGame(@PathVariable gameId: String): Game? = gameRepository.findById(gameId).getOrNull()

  @PostMapping
  fun addGame(@Valid @ModelAttribute addGame: AddGameDTO): Game = gameService.addNewGame(addGame)
}
