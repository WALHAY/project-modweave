package git.walhay.modweave.api.file.repository

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SpringDataFileRepository : JpaRepository<FileEntity, Long> {
  fun findByFilePath(filePath: String): FileEntity?

  fun findAllByVersionId(versionId: UUID): List<FileEntity>
}
