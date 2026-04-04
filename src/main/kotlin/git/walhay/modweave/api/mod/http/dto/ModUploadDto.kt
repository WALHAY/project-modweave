package git.walhay.modweave.api.mod.http.dto

import git.walhay.modweave.api.category.CategoryId
import git.walhay.modweave.api.game.GameId
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.mod.command.ModCreateCommand
import git.walhay.modweave.util.spinalCase
import io.mcarle.konvert.api.KonvertTo
import io.mcarle.konvert.api.Mapping
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.springframework.web.multipart.MultipartFile

@KonvertTo(ModCreateCommand::class, mappings = [Mapping("modId", expression = "modIdGenerated()")])
data class ModUploadDto(
    private var modId: ModId,
    @field:NotBlank @field:Size(min = 3) val name: String,
    val description: String,
    @field:NotNull val image: MultipartFile,
    val categories: Set<CategoryId> = mutableSetOf(),
    @field:NotBlank val versionName: String,
    @field:NotEmpty val files: List<MultipartFile> = mutableListOf(),
    @field:NotBlank val gameId: GameId
) {
  init {
    modId = ModId(name.spinalCase())
  }

  fun modIdGenerated() = modId
}
