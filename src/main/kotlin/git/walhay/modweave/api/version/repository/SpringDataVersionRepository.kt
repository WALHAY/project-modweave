package git.walhay.modweave.api.version.repository

import git.walhay.modweave.api.version.Version
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataVersionRepository : JpaRepository<VersionEntity, Long> {
    fun findAllByModId(modId: String, pageable: Pageable): Page<Version>
}
