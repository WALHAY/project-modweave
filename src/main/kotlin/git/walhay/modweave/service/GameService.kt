package git.walhay.modweave.service

import git.walhay.modweave.dto.AddGameDTO
import git.walhay.modweave.model.Game
import git.walhay.modweave.repository.GameRepository
import git.walhay.modweave.util.spinalCase
import kotlin.jvm.optionals.getOrNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional
class GameService @Autowired constructor(private val gameRepository: GameRepository) {

  fun findGamesWithFilter(page: Int, size: Int, name: String?, sort: Sort): Page<Game> {
    val pageRequest = PageRequest.of(page, size, sort)
    if (name == null) {
      return gameRepository.findAll(pageRequest)
    }
    return gameRepository.findAllByNameContainingIgnoreCase(name, pageRequest)
  }

  fun findGameById(id: String): Game? {
    return gameRepository.findById(id).getOrNull()
  }

  fun addNewGame(game: AddGameDTO): Game {
    // TODO: add image path
    val gameId = game.name.spinalCase()
    if (gameRepository.existsById(gameId)) {
      throw ResponseStatusException(HttpStatus.CONFLICT, "Game with id=$gameId already exist")
    }

    return gameRepository.save(game.toEntity())
  }
}
