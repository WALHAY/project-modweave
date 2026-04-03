package git.walhay.modweave.api.game.repository

import git.walhay.modweave.api.game.Game
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface GameRepository {
  fun findById(id: String): Game?

  fun findAll(pageable: Pageable): Page<Game>

  fun findAll(name: String, pageable: Pageable): Page<Game>

  fun existsById(id: String): Boolean

  fun save(game: Game): Game
}
