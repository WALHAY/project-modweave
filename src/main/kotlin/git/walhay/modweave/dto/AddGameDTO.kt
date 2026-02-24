package git.walhay.modweave.dto

import git.walhay.modweave.model.Game
import io.mcarle.konvert.api.KonvertTo
import io.mcarle.konvert.api.Mapping
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.web.multipart.MultipartFile

@KonvertTo(
    Game::class,
    mappings = [Mapping(source = "id", target = "name", expression = "name.spinalCase()")])
data class AddGameDTO(
    @NotBlank val name: String,
    val description: String,
    @NotNull val image: MultipartFile
)
