package git.walhay.modweave.api.game.http.dto

import git.walhay.modweave.api.game.Game
import git.walhay.modweave.api.game.GameId
import io.mcarle.konvert.api.KonvertFrom

@KonvertFrom(Game::class)
data class GameResponseDto(
    val id: GameId,
    val name: String,
    val description: String?,
    val imagePath: String
) {
  companion object
}
