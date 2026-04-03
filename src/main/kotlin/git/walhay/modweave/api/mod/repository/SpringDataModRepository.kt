package git.walhay.modweave.api.mod.repository

import git.walhay.modweave.api.mod.ModId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataModRepository : JpaRepository<ModEntity, ModId> {
  fun findAllByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<ModEntity>
}
