package git.walhay.modweave.api.file

import git.walhay.modweave.api.file.dto.FileDto
import git.walhay.modweave.api.file.repository.FileEntity
import git.walhay.modweave.api.version.Version
import io.mcarle.konvert.api.KonvertTo

@KonvertTo(FileEntity::class, mapFunctionName = "toEntity")
@KonvertTo(FileDto::class)
data class File(
    val id: Long? = null,
    val filename: String,
    val filePath: String,
    var downloads: Int = 0,
    val version: Version,
    val metainfo: String? = null
) {
  constructor(
      filename: String,
      filePath: String,
      version: Version,
      metainfo: String? = null
  ) : this(null, filename, filePath, 0, version, metainfo)

  constructor() : this("", "", Version(), null)
}
