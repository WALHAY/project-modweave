package git.walhay.modweave.api.game.repository

import git.walhay.modweave.api.game.GameId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataGameRepository : JpaRepository<GameEntity, GameId> {
  fun findAllByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<GameEntity>
}
