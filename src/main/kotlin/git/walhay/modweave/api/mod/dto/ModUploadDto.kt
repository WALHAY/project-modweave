package git.walhay.modweave.api.mod.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.springframework.web.multipart.MultipartFile

data class ModUploadDto(
	@field:NotBlank @field:Size(min = 3) val name: String,
	val description: String,
	@field:NotNull val image: MultipartFile,
	val categories: Set<String> = mutableSetOf(),
	@field:NotBlank val versionName: String,
	@field:NotEmpty val files: List<MultipartFile> = mutableListOf(),
	@field:NotBlank val game: String
)
