package git.walhay.modweave.api.game

import git.walhay.modweave.api.game.dto.GameUploadDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort

interface IGameService {
  fun findGameById(gameId: GameId): Game

  fun findGamesWithFilter(page: Int, size: Int, name: String?, sort: Sort): Page<Game>

  fun uploadGame(dto: GameUploadDto): Game
}
