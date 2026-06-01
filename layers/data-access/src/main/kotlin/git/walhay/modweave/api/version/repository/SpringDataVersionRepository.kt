package git.walhay.modweave.api.version.repository

import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataVersionRepository : JpaRepository<VersionEntity, UUID> {
  fun findAllByModId(
      modId: String,
      pageable: Pageable,
  ): Page<VersionEntity>
}
