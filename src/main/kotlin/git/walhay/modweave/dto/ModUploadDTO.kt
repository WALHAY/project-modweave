package git.walhay.modweave.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.springframework.web.multipart.MultipartFile

data class ModUploadDTO(
    @NotBlank val name: String,
    val description: String,
    val categories: Set<String> = mutableSetOf(),
    @NotBlank val versionName: String,
    @NotEmpty val files: List<MultipartFile> = mutableListOf(),
    @NotBlank val game: Long
)
