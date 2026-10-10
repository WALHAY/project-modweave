package git.walhay.modweave.domain.game

import org.springframework.web.multipart.MultipartFile

data class GameCreateCommand(
    val id: GameId,
    val name: String,
    val description: String?,
    val image: MultipartFile,
)
