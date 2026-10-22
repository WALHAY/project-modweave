package git.walhay.modweave.domain.mod

import git.walhay.modweave.domain.category.CategoryId
import git.walhay.modweave.domain.game.GameId
import git.walhay.modweave.domain.user.UserId
import git.walhay.modweave.domain.version.Version
import java.io.Serializable
import java.time.LocalDateTime

data class Mod(
    val id: ModId,
    val name: String,
    val description: String? = null,
    val imagePath: String,
    val creationDate: LocalDateTime = LocalDateTime.now(),
    val publisherId: UserId,
    val gameId: GameId,
    val categories: Set<CategoryId> = emptySet(),
    val versions: MutableList<Version> = mutableListOf(),
) : Serializable
