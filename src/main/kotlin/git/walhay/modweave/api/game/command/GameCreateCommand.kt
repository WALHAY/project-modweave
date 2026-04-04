package git.walhay.modweave.api.game.command

import git.walhay.modweave.api.game.GameId
import org.springframework.web.multipart.MultipartFile

data class GameCreateCommand(
    val id: GameId,
    val name: String,
    val description: String?,
    val image: MultipartFile
)
