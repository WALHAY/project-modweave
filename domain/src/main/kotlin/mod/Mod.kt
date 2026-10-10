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
) : Serializable {
  constructor(
      id: ModId,
      name: String,
      description: String? = null,
      imagePath: String,
      publisherId: UserId,
      gameId: GameId,
      categories: Set<CategoryId> = emptySet(),
      versions: MutableList<Version> = mutableListOf(),
  ) : this(
      id = id,
      name = name,
      description = description,
      imagePath = imagePath,
      creationDate = LocalDateTime.now(),
      publisherId = publisherId,
      gameId = gameId,
      categories = categories,
      versions = versions,
  )
}
