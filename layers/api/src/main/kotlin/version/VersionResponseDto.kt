package git.walhay.modweave.api.version

import git.walhay.modweave.api.common.ApiResource
import git.walhay.modweave.domain.version.Version
import java.time.LocalDateTime
import java.util.UUID

data class VersionResponseDto<T>(
    val id: UUID,
    val files: List<T>,
    val name: String,
    val changes: String?,
    val uploadDate: LocalDateTime,
    val status: String,
    val modId: String,
) : ApiResource {
  companion object {
    fun fromVersion(version: Version): VersionResponseDto<FileResponseDto> =
        VersionResponseDto(
            id = version.id.value,
            files = version.files.map { FileResponseDto(it.id.value, it.filename, it.downloads) },
            name = version.name,
            changes = version.changes,
            uploadDate = version.uploadDate,
            status = version.status.toString(),
            modId = version.modId.value)
  }
}

data class FileResponseDto(
    val id: UUID,
    val filename: String,
    val downloads: Int,
) : ApiResource
