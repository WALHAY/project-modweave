package git.walhay.modweave.api.game

import git.walhay.modweave.api.game.dto.UploadGameDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort

interface IGameService {
  fun findGameById(modId: String): Game

  fun findGamesWithFilter(page: Int, size: Int, name: String?, sort: Sort): Page<Game>

  fun uploadGame(dto: UploadGameDto): Game
}
