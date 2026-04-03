package git.walhay.modweave.api.game.dto

import git.walhay.modweave.api.game.GameId

data class GameResponseDto(
    val id: GameId,
    val name: String,
    val description: String?,
    val imagePath: String
)
