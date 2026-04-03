package git.walhay.modweave.api.version.repository

import git.walhay.modweave.api.version.VersionId
import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataVersionRepository : JpaRepository<VersionEntity, VersionId>
