package git.walhay.modweave.api.file.repository

import git.walhay.modweave.api.file.File
import org.springframework.stereotype.Repository

@Repository
class JpaFileRepository(private val repository: SpringDataFileRepository) : FileRepository {
  override fun save(file: File): File = repository.save(FileEntity.fromFile(file)).toDomain()
}
