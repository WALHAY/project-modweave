package git.walhay.modweave.domain.file

import git.walhay.modweave.domain.version.VersionId
import java.io.Serializable

data class File(
    val id: FileId,
    val filename: String,
    val filePath: String,
    var downloads: Int = 0,
    val versionId: VersionId,
) : Serializable {
  constructor(
      filename: String,
      filePath: String,
      versionId: VersionId,
  ) : this(FileId(), filename, filePath, 0, versionId)
}
