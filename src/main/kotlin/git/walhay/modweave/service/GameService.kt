package git.walhay.modweave.service

import git.walhay.modweave.dto.AddGameDTO
import git.walhay.modweave.dto.GameDto
import git.walhay.modweave.dto.toGame
import git.walhay.modweave.exception.GameNotFoundException
import git.walhay.modweave.model.toGameDto
import git.walhay.modweave.repository.GameRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional
class GameService(private val gameRepository: GameRepository) {
  fun findGameById(modId: String): GameDto =
      gameRepository
          .findById(modId)
          .orElseThrow { GameNotFoundException("Game with id=$modId not found") }
          .toGameDto()

  fun findGamesWithFilter(page: Int, size: Int, name: String?, sort: Sort): Page<GameDto> {
    val pageRequest = PageRequest.of(page, size, sort)
    if (name == null) {
      return gameRepository.findAll(pageRequest).map { it.toGameDto() }
    }
    return gameRepository.findAllByNameContainingIgnoreCase(name, pageRequest).map {
      it.toGameDto()
    }
  }

  fun uploadGame(dto: AddGameDTO): GameDto {
    // TODO: add image path
    val game = dto.toGame()
    if (gameRepository.existsById(game.id)) {
      throw ResponseStatusException(HttpStatus.CONFLICT, "Game with id=${game.id} already exist")
    }

    return gameRepository.save(game).toGameDto()
  }
}
