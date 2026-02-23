package git.walhay.modweave.controller

import git.walhay.modweave.dto.AddGameDTO
import git.walhay.modweave.model.Game
import git.walhay.modweave.service.GameService
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.data.domain.Sort
import org.springframework.data.web.SortDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/games")
class GameController(private val gameService: GameService) {

  @GetMapping
  fun getGames(
      @RequestParam @Min(0) page: Int,
      @RequestParam @Min(1) size: Int,
      @RequestParam(required = false) name: String?,
      @SortDefault(sort = ["name"]) sort: Sort
  ) = gameService.findGamesWithFilter(page, size, name, sort)

  @GetMapping("/{gameId}")
  fun getGame(@PathVariable gameId: String): Game = gameService.findGameById(gameId)

  @PostMapping
  fun addGame(@Valid @ModelAttribute addGame: AddGameDTO): ResponseEntity<Game> {
    val game = gameService.uploadGame(addGame)
    return ResponseEntity.status(HttpStatus.CREATED).body(game)
  }
}
