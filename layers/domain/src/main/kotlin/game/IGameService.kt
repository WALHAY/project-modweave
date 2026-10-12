package git.walhay.modweave.domain.game

import git.walhay.modweave.domain.common.paging.Page
import org.springframework.data.domain.Sort

interface IGameService {
  fun findGameById(gameId: GameId): Game

  fun findGamesWithFilter(
      page: Int,
      size: Int,
      name: String?,
      sort: Sort,
  ): Page<Game>

  fun uploadGame(command: GameCreateCommand): Game

  fun deleteGame(gameId: GameId)
}
