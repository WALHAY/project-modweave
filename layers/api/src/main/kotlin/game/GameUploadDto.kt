package git.walhay.modweave.api.game

import git.walhay.modweave.api.util.ValidImage
import git.walhay.modweave.domain.game.GameCreateCommand
import git.walhay.modweave.domain.game.GameId
import git.walhay.modweave.domain.util.spinalCase
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.web.multipart.MultipartFile

data class GameUploadDto(
    @field:NotBlank val name: String,
    val description: String?,
    @field:NotNull @field:ValidImage val image: MultipartFile,
) {
  fun toGameCreateCommand() =
      GameCreateCommand(
          id = GameId(name.spinalCase()), name = name, description = description, image = image)
}
