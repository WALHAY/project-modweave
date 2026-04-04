package git.walhay.modweave.api.game

import git.walhay.modweave.api.game.http.dto.GameResponseDto
import git.walhay.modweave.api.game.repository.GameEntity
import git.walhay.modweave.api.mod.Mod
import io.mcarle.konvert.api.KonvertTo

@KonvertTo(GameEntity::class, mapFunctionName = "toEntity")
@KonvertTo(GameResponseDto::class)
class Game(
    val id: GameId = GameId(),
    val name: String = "",
    val description: String? = null,
    var imagePath: String = "",
    val mods: MutableList<Mod> = mutableListOf()
)
