package git.walhay.modweave.api.game

import git.walhay.modweave.api.game.dto.GameResponseDto
import git.walhay.modweave.api.game.repository.GameEntity
import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.util.spinalCase
import io.mcarle.konvert.api.KonvertTo

@KonvertTo(GameEntity::class, mapFunctionName = "toEntity")
@KonvertTo(GameResponseDto::class)
class Game(
    val id: GameId,
    val name: String,
    val description: String? = null,
    var imagePath: String,
    val mods: MutableList<Mod> = mutableListOf()
) {
  constructor() : this("", null, "")

  constructor(name: String, description: String? = null) : this(name, description, "")

  constructor(
      name: String,
      description: String? = null,
      imagePath: String
  ) : this(
      id = GameId(name.spinalCase()), name = name, description = description, imagePath = imagePath)
}
