package git.walhay.modweave.repositories

import git.walhay.modweave.models.Game
import git.walhay.modweave.models.Mod
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ModRepository : CrudRepository<Mod, Long> {

  fun findByGame(game: Game): Set<Mod>

  fun findAll(pageable: Pageable): List<Mod>
}
