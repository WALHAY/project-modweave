package git.walhay.modweave.domain.game

import git.walhay.modweave.domain.common.paging.Page
import org.springframework.data.domain.Pageable

interface GameRepository {
  fun findById(gameId: GameId): Game?

  fun findAll(pageable: Pageable): Page<Game>

  fun findAll(
      name: String,
      pageable: Pageable,
  ): Page<Game>

  fun existsByIdIgnoreCase(gameId: GameId): Boolean

  fun save(game: Game): Game

  fun deleteById(gameId: GameId)
}
