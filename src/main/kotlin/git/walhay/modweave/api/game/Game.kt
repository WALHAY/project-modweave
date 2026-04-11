package git.walhay.modweave.api.game

import git.walhay.modweave.api.mod.Mod
import java.io.Serializable

class Game(
    val id: GameId = GameId(),
    val name: String = "",
    val description: String? = null,
    var imagePath: String = "",
    val mods: MutableList<Mod> = mutableListOf()
) : Serializable
