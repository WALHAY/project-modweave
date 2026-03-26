package git.walhay.modweave.api.version.repository

import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataVersionRepository : JpaRepository<VersionEntity, Long> {}
