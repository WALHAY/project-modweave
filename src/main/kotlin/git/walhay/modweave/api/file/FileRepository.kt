package git.walhay.modweave.api.file

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository interface FileRepository : JpaRepository<File, Long>
