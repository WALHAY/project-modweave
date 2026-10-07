package git.walhay.modweave.api.game.http

import git.walhay.modweave.api.common.paging.Page
import git.walhay.modweave.api.game.GameId
import git.walhay.modweave.api.game.IGameService
import git.walhay.modweave.api.game.http.dto.GameResponseDto
import git.walhay.modweave.api.game.http.dto.GameUploadDto
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import java.net.URI
import mu.KLogger
import mu.KotlinLogging
import org.springframework.data.domain.Sort
import org.springframework.data.web.SortDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/games")
class GameController(
    private val gameService: IGameService,
) {
  private val logger: KLogger = KotlinLogging.logger {}

  @GetMapping
  fun getGames(
      @RequestParam @Min(0) page: Int,
      @RequestParam @Min(1) size: Int,
      @RequestParam(required = false) name: String?,
      @SortDefault(sort = ["name"]) sort: Sort,
  ): Page<GameResponseDto> {
    logger.debug { "GET /games - page: $page, size: $size, name: $name" }
    return gameService.findGamesWithFilter(page, size, name, sort).map {
      GameResponseDto.fromGame(it)
    }
  }

  @GetMapping("/{gameId}")
  fun getGame(
      @PathVariable gameId: String,
  ): GameResponseDto {
    logger.info { "GET /games/$gameId" }
    return gameService.findGameById(GameId(gameId)).let { GameResponseDto.fromGame(it) }
  }

  @PostMapping(version = "1", consumes = ["multipart/form-data"])
  @ResponseStatus(HttpStatus.CREATED)
  fun addGame(
      @Valid @ModelAttribute dto: GameUploadDto,
  ): GameResponseDto {
    logger.info { "POST /games - uploading game: ${dto.name}" }
    return gameService.uploadGame(dto.toGameCreateCommand()).let { GameResponseDto.fromGame(it) }
  }

  @PostMapping(version = "2", consumes = ["multipart/form-data"])
  fun createGame(@Valid @ModelAttribute dto: GameUploadDto): ResponseEntity<GameResponseDto> {
    val result = addGame(dto)
    return ResponseEntity.created(URI.create("/api/v2/games/${result.id}")).body(result)
  }

  @DeleteMapping("/{gameId}", version = "1")
  @ResponseStatus(HttpStatus.OK)
  fun deleteGame(@PathVariable gameId: String) {
    logger.info { "DELETE /games - deleting game: $gameId" }
    gameService.deleteGame(GameId(gameId))
  }

  @DeleteMapping("/{gameId}", version = "2")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun removeGame(@PathVariable gameId: String) = deleteGame(gameId)
}
