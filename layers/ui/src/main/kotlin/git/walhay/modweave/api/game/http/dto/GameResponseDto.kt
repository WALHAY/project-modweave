package git.walhay.modweave.api.game.http.dto

import git.walhay.modweave.api.game.Game
import git.walhay.modweave.api.game.GameId

data class GameResponseDto(
    val id: GameId,
    val name: String,
    val description: String?,
    val imagePath: String,
) {
  companion object {
    fun fromGame(game: Game): GameResponseDto =
        GameResponseDto(
            id = game.id,
            name = game.name,
            description = game.description,
            imagePath = game.imagePath,
        )
  }
}
