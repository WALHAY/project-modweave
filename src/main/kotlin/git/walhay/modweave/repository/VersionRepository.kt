package git.walhay.modweave.repository

import git.walhay.modweave.model.Version
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository interface VersionRepository : JpaRepository<Version, Long>
