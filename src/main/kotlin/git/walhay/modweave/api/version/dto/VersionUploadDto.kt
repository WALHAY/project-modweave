package git.walhay.modweave.api.version.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.springframework.web.multipart.MultipartFile

data class VersionUploadDto(
    @NotBlank val name: String,
    val changes: String,
    @NotEmpty val files: List<MultipartFile>
)
