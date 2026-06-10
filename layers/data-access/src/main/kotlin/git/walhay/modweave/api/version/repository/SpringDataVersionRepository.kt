package git.walhay.modweave.api.version.repository

import git.walhay.modweave.api.version.VersionStatus
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataVersionRepository : JpaRepository<VersionEntity, UUID> {
  fun findAllByModId(
      modId: String,
      pageable: Pageable,
  ): Page<VersionEntity>

  fun findAllByModIdAndStatus(
      modId: String,
      status: VersionStatus,
      pageable: Pageable,
  ): Page<VersionEntity>
}
