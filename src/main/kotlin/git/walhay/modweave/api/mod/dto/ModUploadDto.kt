package git.walhay.modweave.api.mod.dto

import git.walhay.modweave.api.mod.Mod
import io.mcarle.konvert.api.KonvertTo
import io.mcarle.konvert.api.Mapping
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.springframework.web.multipart.MultipartFile

@KonvertTo(
    Mod::class,
    mappings = [Mapping(source = "id", target = "name", expression = "name.spinalCase()")])
data class ModUploadDto(
    @NotBlank @Size(min = 3) val name: String,
    val description: String,
    @NotNull val image: MultipartFile,
    val categories: Set<String> = mutableSetOf(),
    @NotBlank val versionName: String,
    @NotEmpty val files: List<MultipartFile> = mutableListOf(),
    @NotBlank val game: String
)
