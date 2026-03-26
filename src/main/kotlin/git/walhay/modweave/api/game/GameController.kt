package git.walhay.modweave.api.game

import git.walhay.modweave.api.game.dto.AddGameDto
import git.walhay.modweave.api.game.dto.GameDto
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort
import org.springframework.data.web.SortDefault
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/games")
class GameController(private val gameService: IGameService) {

  @GetMapping
  fun getGames(
      @RequestParam @Min(0) page: Int,
      @RequestParam @Min(1) size: Int,
      @RequestParam(required = false) name: String?,
      @SortDefault(sort = ["name"]) sort: Sort
  ): Page<GameDto> = gameService.findGamesWithFilter(page, size, name, sort).map { it.toGameDto() }

  @GetMapping("/{gameId}")
  fun getGame(@PathVariable gameId: String): GameDto = gameService.findGameById(gameId).toGameDto()

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  fun addGame(@Valid @ModelAttribute addGame: AddGameDto): GameDto =
      gameService.uploadGame(addGame).toGameDto()
}
