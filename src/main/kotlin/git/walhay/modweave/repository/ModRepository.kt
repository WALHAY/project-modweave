package git.walhay.modweave.repository

import git.walhay.modweave.model.Game
import git.walhay.modweave.model.Mod
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ModRepository : CrudRepository<Mod, String> {

  fun findByGame(game: Game): Set<Mod>

  fun findAll(pageable: Pageable): List<Mod>
}
