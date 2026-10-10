package git.walhay.modweave.api.mod

import git.walhay.modweave.api.util.ValidImage
import git.walhay.modweave.domain.category.CategoryId
import git.walhay.modweave.domain.game.GameId
import git.walhay.modweave.domain.mod.ModCreateCommand
import git.walhay.modweave.domain.mod.ModId
import git.walhay.modweave.domain.util.spinalCase
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.springframework.web.multipart.MultipartFile

data class ModUploadDto(
    @field:NotBlank @field:Size(min = 3, max = 50) val name: String,
    val description: String?,
    @field:NotNull @field:ValidImage val image: MultipartFile,
    val categories: Set<String> = mutableSetOf(),
    @field:NotBlank val versionName: String,
    @field:NotEmpty val files: List<MultipartFile> = mutableListOf(),
    @field:NotBlank val gameId: String,
) {
  fun toModCreateCommand() =
      ModCreateCommand(
          id = ModId(name.spinalCase()),
          name = name,
          description = description,
          image = image,
          categories = categories.map { CategoryId(it) }.toSet(),
          versionName = versionName,
          files = files,
          gameId = GameId(gameId),
      )
}
