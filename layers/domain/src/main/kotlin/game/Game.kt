package git.walhay.modweave.domain.game

import git.walhay.modweave.domain.mod.Mod
import java.io.Serializable

class Game(
    val id: GameId = GameId(),
    val name: String = "",
    val description: String? = null,
    var imagePath: String = "",
    val mods: MutableList<Mod> = mutableListOf(),
) : Serializable
