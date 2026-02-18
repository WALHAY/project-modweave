package git.walhay.modweave.repositories

import git.walhay.modweave.models.Game
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface GameRepository : CrudRepository<Game, Long> {

  fun findByName(name: String): Game

    fun findAll(pageable: Pageable): Page<Game>
}