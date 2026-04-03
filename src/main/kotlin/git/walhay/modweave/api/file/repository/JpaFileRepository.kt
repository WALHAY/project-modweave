package git.walhay.modweave.api.file.repository

import git.walhay.modweave.api.file.File
import git.walhay.modweave.api.file.toEntity
import org.springframework.stereotype.Repository

@Repository
class JpaFileRepository(private val repository: SpringDataFileRepository) : FileRepository {
  override fun save(file: File): File = repository.save(file.toEntity()).toModel()
}
