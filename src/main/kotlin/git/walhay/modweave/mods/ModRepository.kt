package git.walhay.modweave.mods

import git.walhay.modweave.games.Game
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ModRepository : CrudRepository<Mod, Long> {

    fun findByGame(game: Game): Set<Mod>

    fun findAll(pageable: Pageable): List<Mod>
}