package git.walhay.modweave.api.mod

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ModRepository : JpaRepository<Mod, String> {

	fun findAllByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<Mod>
}
