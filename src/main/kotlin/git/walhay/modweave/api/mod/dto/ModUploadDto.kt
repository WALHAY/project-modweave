package git.walhay.modweave.api.mod.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.springframework.web.multipart.MultipartFile

data class ModUploadDto(
    @NotBlank @Size(min = 3) val name: String,
    val description: String,
    @NotNull val image: MultipartFile,
    val categories: Set<String> = mutableSetOf(),
    @NotBlank val versionName: String,
    @NotEmpty val files: List<MultipartFile> = mutableListOf(),
    @NotBlank val game: String
)
