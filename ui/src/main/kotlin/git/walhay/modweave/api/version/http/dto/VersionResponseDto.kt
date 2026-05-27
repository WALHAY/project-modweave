package git.walhay.modweave.api.version.http.dto

import git.walhay.modweave.api.file.File
import git.walhay.modweave.api.version.Version
import java.time.LocalDateTime

data class VersionResponseDto(
    val id: String,
    val name: String,
    val changes: String?,
    val uploadDate: LocalDateTime,
    val files: List<VersionFileResponseDto>,
) {
  companion object {
    fun fromVersion(version: Version): VersionResponseDto =
        VersionResponseDto(
            id = version.id.value.toString(),
            name = version.name,
            changes = version.changes,
            uploadDate = version.uploadDate,
            files = version.files.map { VersionFileResponseDto.fromFile(it) },
        )
  }
}

data class VersionFileResponseDto(
    val id: Long,
    val filename: String,
    val filePath: String,
) {
  companion object {
    fun fromFile(file: File): VersionFileResponseDto =
        VersionFileResponseDto(
            id = file.id.value,
            filename = file.filename,
            filePath = file.filePath,
        )
  }
}
