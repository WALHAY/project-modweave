package git.walhay.modweave.domain.version

import org.springframework.web.multipart.MultipartFile

data class VersionCreateCommand(
    val name: String,
    val changes: String?,
    val files: List<MultipartFile>,
)
