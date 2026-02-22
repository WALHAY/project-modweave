package git.walhay.modweave.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.springframework.web.multipart.MultipartFile

data class VersionUploadDTO(
    @NotBlank val name: String,
    val changes: String,
    @NotEmpty val files: List<MultipartFile>
)
