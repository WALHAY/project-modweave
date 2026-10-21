package git.walhay.modweave.domain.version

import git.walhay.modweave.domain.file.File
import git.walhay.modweave.domain.mod.ModId
import java.io.Serializable
import java.time.LocalDateTime

data class Version(
    val id: VersionId,
    val name: String,
    val changes: String? = null,
    val uploadDate: LocalDateTime,
    var status: VersionStatus,
    val modId: ModId,
    val files: MutableList<File> = mutableListOf(),
) : Serializable {
  constructor(
      name: String,
      changes: String? = null,
      modId: ModId,
  ) : this(
      id = VersionId(),
      name = name,
      changes = changes,
      uploadDate = LocalDateTime.now(),
      status = VersionStatus.PENDING,
      modId = modId,
      files = mutableListOf(),
  )
}
