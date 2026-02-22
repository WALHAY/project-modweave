package git.walhay.modweave.dto

import git.walhay.modweave.model.Game
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.web.multipart.MultipartFile

data class AddGameDTO(
    @NotBlank val name: String,
    val description: String,
    @NotNull val image: MultipartFile
) {
  fun toEntity() = Game(name, description, image.name)
}
