package git.walhay.modweave.api.version.dto

import git.walhay.modweave.api.file.dto.FileDto
import java.time.LocalDateTime

data class VersionDto(
    val name: String,
    val changes: String?,
    val uploadDate: LocalDateTime,
    val downloads: Long,
    val files: List<FileDto>
)
