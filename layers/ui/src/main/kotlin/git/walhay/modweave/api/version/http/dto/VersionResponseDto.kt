package git.walhay.modweave.api.version.http.dto

import git.walhay.modweave.api.version.Version
import java.time.LocalDateTime
import java.util.UUID

data class VersionResponseDto(
    val id: UUID,
    val files: List<FileResponseDto>,
    val name: String,
    val changes: String?,
    val uploadDate: LocalDateTime,
    val status: String
) {
  companion object {
    fun fromVersion(version: Version): VersionResponseDto =
        VersionResponseDto(
            id = version.id.value,
            files = version.files.map { FileResponseDto(it.id.value, it.filename, it.downloads) },
            name = version.name,
            changes = version.changes,
            uploadDate = version.uploadDate,
            status = version.status.toString())
  }
}

data class FileResponseDto(val id: UUID, val filename: String, val downloads: Int)
