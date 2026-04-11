package git.walhay.modweave.api.game.repository

import git.walhay.modweave.api.game.Game
import git.walhay.modweave.api.game.GameId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface GameRepository {
  fun findById(gameId: GameId): Game?

  fun findAll(pageable: Pageable): Page<Game>

  fun findAll(name: String, pageable: Pageable): Page<Game>

  fun existsByIdIgnoreCase(gameId: GameId): Boolean

  fun save(game: Game): Game

  fun deleteById(gameId: GameId)
}
