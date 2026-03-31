package git.walhay.modweave.api.game.dto

data class GameResponseDto(
    val id: String,
    val name: String,
    val description: String?,
    val imagePath: String
)
