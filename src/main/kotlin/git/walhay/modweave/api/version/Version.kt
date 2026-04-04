package git.walhay.modweave.api.version

import git.walhay.modweave.api.file.File
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.version.http.dto.VersionResponseDto
import git.walhay.modweave.api.version.repository.VersionEntity
import io.mcarle.konvert.api.KonvertTo
import java.time.LocalDateTime

@KonvertTo(VersionEntity::class, mapFunctionName = "toEntity")
@KonvertTo(VersionResponseDto::class)
data class Version(
    val id: VersionId,
    val name: String,
    val changes: String? = null,
    val uploadDate: LocalDateTime,
    val status: VersionStatus,
    val modId: ModId,
    val files: MutableList<File> = mutableListOf()
) {
  constructor(
      name: String,
      changes: String? = null,
      modId: ModId
  ) : this(
      id = VersionId(),
      name = name,
      changes = changes,
      uploadDate = LocalDateTime.now(),
      status = VersionStatus.PENDING,
      modId = modId,
      files = mutableListOf())

  constructor() : this("", null, ModId())
}
