package git.walhay.modweave.games

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface GameRepository : CrudRepository<Game, Long> {

    fun findByName(name: String): Game
}