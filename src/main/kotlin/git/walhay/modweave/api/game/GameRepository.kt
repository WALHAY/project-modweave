package git.walhay.modweave.api.game

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GameRepository : JpaRepository<Game, String> {

  fun findAllByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<Game>
}
