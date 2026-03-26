package git.walhay.modweave.api.file.repository

import org.springframework.stereotype.Repository

@Repository
class JpaFileRepository(private val repository: SpringDataFileRepository) : FileRepository {}
