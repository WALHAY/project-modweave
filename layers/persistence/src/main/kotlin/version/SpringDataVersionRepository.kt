package git.walhay.modweave.persistence.version

import git.walhay.modweave.domain.version.VersionStatus
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
