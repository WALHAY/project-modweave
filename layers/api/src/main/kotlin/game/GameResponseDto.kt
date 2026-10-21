package git.walhay.modweave.api.game

import git.walhay.modweave.api.common.ApiResource
import git.walhay.modweave.domain.game.Game

data class GameResponseDto(
    val id: String,
    val name: String,
    val description: String?,
    val imagePath: String,
) : ApiResource {
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
