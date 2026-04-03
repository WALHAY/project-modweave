package git.walhay.modweave.api.game.dto

import git.walhay.modweave.util.ValidImage
import git.walhay.modweave.util.spinalCase
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.web.multipart.MultipartFile

data class GameUploadDto(
    @field:NotBlank val name: String,
    val description: String?,
    @field:NotNull @field:ValidImage val image: MultipartFile
) {
  val nameSpinal = name.spinalCase()
}
