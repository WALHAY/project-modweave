package git.walhay.modweave.domain.version

import org.springframework.web.multipart.MultipartFile

data class VersionCreateCommand(
    val name: String,
    val changes: String?,
    val files: List<MultipartFile>,
)

class VersionExistsException(
    version: String,
) : RuntimeException("Version with name '$version' already exists")

class VersionNotFoundException(
    versionId: VersionId,
) : RuntimeException("Version with id=\"$versionId\" not found")
