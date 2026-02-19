package git.walhay.modweave.repositories

import git.walhay.modweave.models.Version
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VersionRepository : JpaRepository<Version, String>