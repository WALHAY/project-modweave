package git.walhay.modweave.domain.game

import git.walhay.modweave.domain.common.paging.Page
import git.walhay.modweave.domain.common.paging.PageRequest

interface GameRepository {
  fun findById(gameId: GameId): Game?

  fun findAll(pageRequest: PageRequest): Page<Game>

  fun findAll(
      name: String,
      pageRequest: PageRequest,
  ): Page<Game>

  fun existsByIdIgnoreCase(gameId: GameId): Boolean

  fun save(game: Game): Game

  fun deleteById(gameId: GameId)
}
