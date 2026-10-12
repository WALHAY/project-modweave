package git.walhay.modweave.persistence.game

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataGameRepository : JpaRepository<GameEntity, String> {
  fun findAllByNameContainingIgnoreCase(
      name: String,
      pageable: Pageable,
  ): Page<GameEntity>

  fun existsByIdIgnoreCase(id: String): Boolean
}
