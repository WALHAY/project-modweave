package git.walhay.modweave.api.mod.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataModRepository : JpaRepository<ModEntity, String> {
  fun findAllByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<ModEntity>
}
