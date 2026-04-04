package git.walhay.modweave.api.game.http

import git.walhay.modweave.api.game.GameId
import git.walhay.modweave.api.game.IGameService
import git.walhay.modweave.api.game.http.dto.GameResponseDto
import git.walhay.modweave.api.game.http.dto.GameUploadDto
import git.walhay.modweave.api.game.toGameResponseDto
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
  ): Page<GameResponseDto> =
      gameService.findGamesWithFilter(page, size, name, sort).map { it.toGameResponseDto() }

  @GetMapping("/{gameId}")
  fun getGame(@PathVariable gameId: GameId): GameResponseDto =
      gameService.findGameById(gameId).toGameResponseDto()

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  fun addGame(@Valid @ModelAttribute dto: GameUploadDto): GameResponseDto =
      gameService.uploadGame(dto.toGameCreateCommand()).toGameResponseDto()
}
