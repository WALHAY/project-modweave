package git.walhay.modweave.api.version.dto

import java.time.LocalDateTime

data class VersionDto(
    val name: String,
    val changes: String?,
    val uploadDate: LocalDateTime,
)
