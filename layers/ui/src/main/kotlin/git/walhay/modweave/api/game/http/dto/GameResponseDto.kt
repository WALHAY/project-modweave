package git.walhay.modweave.api.game.http.dto

import git.walhay.modweave.api.game.Game
import git.walhay.modweave.api.game.GameId

data class GameResponseDto(
    val id: String,
    val name: String,
    val description: String?,
    val imagePath: String,
) {
  companion object {
    fun fromGame(game: Game): GameResponseDto =
        GameResponseDto(
            id = game.id.value,
            name = game.name,
            description = game.description,
            imagePath = game.imagePath,
        )
  }
}
