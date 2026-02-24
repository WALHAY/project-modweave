package git.walhay.modweave.dto

import java.time.LocalDateTime

data class VersionDto(
    val name: String,
    val changes: String?,
    val uploadDate: LocalDateTime,
    val downloads: Long,
    val files: List<FileDto>
)
