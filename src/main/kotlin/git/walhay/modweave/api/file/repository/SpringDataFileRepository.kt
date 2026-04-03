package git.walhay.modweave.api.file.repository

import git.walhay.modweave.api.file.FileId
import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataFileRepository : JpaRepository<FileEntity, FileId>
