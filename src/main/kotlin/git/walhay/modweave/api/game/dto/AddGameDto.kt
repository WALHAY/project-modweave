package git.walhay.modweave.api.game.dto

import git.walhay.modweave.api.game.Game
import io.mcarle.konvert.api.KonvertTo
import io.mcarle.konvert.api.Mapping
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.web.multipart.MultipartFile

@KonvertTo(
    Game::class,
    mappings = [Mapping(source = "id", target = "name", expression = "name.spinalCase()")])
data class AddGameDto(
    @NotBlank val name: String,
    val description: String,
    @NotNull val image: MultipartFile
)
