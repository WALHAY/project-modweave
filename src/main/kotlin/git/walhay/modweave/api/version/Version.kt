package git.walhay.modweave.api.version

import git.walhay.modweave.api.file.File
import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.api.version.dto.VersionDto
import git.walhay.modweave.api.version.repository.VersionEntity
import io.mcarle.konvert.api.KonvertTo
import java.time.LocalDateTime

@KonvertTo(VersionEntity::class, mapFunctionName = "toEntity")
@KonvertTo(VersionDto::class)
data class Version(
    val id: Long? = null,
    val name: String,
    val changes: String? = null,
    val uploadDate: LocalDateTime,
    val approved: Boolean = false,
    val mod: Mod,
    val files: MutableList<File> = mutableListOf()
) {
  constructor(
      name: String,
      changes: String? = null,
      mod: Mod
  ) : this(
      name = name,
      changes = changes,
      uploadDate = LocalDateTime.now(),
      approved = false,
      mod = mod,
      files = mutableListOf())

  constructor() : this("", null, Mod())
}
