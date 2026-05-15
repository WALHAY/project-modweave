package git.walhay.modweave.api.file.repository

import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataFileRepository : JpaRepository<FileEntity, Long> {
  fun findByFilePath(filePath: String): FileEntity?

  fun findAllByVersionId(versionId: UUID): List<FileEntity>
}
