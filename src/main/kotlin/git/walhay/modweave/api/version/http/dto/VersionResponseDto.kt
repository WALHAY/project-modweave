package git.walhay.modweave.api.version.http.dto

import java.time.LocalDateTime

data class VersionResponseDto(
    val name: String,
    val changes: String?,
    val uploadDate: LocalDateTime,
)
