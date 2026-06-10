package git.walhay.modweave.api.version.http.dto

import git.walhay.modweave.api.version.Version
import java.time.LocalDateTime

data class VersionResponseDto(
    val name: String,
    val changes: String?,
    val uploadDate: LocalDateTime,
    val status: String
) {
  companion object {
    fun fromVersion(version: Version): VersionResponseDto =
        VersionResponseDto(
            name = version.name,
            changes = version.changes,
            uploadDate = version.uploadDate,
            status = version.status.toString())
  }
}
