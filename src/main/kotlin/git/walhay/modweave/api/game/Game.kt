package git.walhay.modweave.api.game

import git.walhay.modweave.api.game.dto.GameDto
import git.walhay.modweave.api.game.repository.GameEntity
import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.util.spinalCase
import io.mcarle.konvert.api.KonvertTo

@KonvertTo(GameEntity::class, mapFunctionName = "toEntity")
@KonvertTo(GameDto::class)
class Game(
    val id: String,
    val name: String,
    val description: String? = null,
    var imagePath: String,
    val mods: MutableList<Mod> = mutableListOf()
) {
  constructor() : this("", null, "")

  constructor(
      name: String,
      description: String? = null
  ) : this(id = name.spinalCase(), name = name, description = description, "")

  constructor(
      name: String,
      description: String? = null,
      imagePath: String
  ) : this(id = name.spinalCase(), name = name, description = description, imagePath = imagePath)
}
