package git.walhay.modweave.api.file.dto

import git.walhay.modweave.api.version.VersionId

data class FileDto(val filename: String, val filePath: String, val versionId: VersionId)
