package git.walhay.modweave.api.file.repository

import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional

interface SpringDataFileRepository : JpaRepository<FileEntity, UUID> {
  @Transactional
  @Modifying
  @Query("update FileEntity f set f.downloads = f.downloads + 1 where f.id = :id")
  fun incrementDownloads(id: UUID)

  fun findByFilePath(filePath: String): FileEntity?

  fun findAllByVersionId(versionId: UUID): List<FileEntity>
}
