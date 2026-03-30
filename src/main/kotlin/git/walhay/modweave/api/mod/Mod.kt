package git.walhay.modweave.api.mod

import git.walhay.modweave.api.category.CategoryId
import git.walhay.modweave.api.game.GameId
import git.walhay.modweave.api.mod.dto.ModDto
import git.walhay.modweave.api.mod.repository.ModEntity
import git.walhay.modweave.api.user.UserId
import git.walhay.modweave.api.version.Version
import git.walhay.modweave.util.spinalCase
import io.mcarle.konvert.api.KonvertTo
import java.time.LocalDateTime

@KonvertTo(ModEntity::class, mapFunctionName = "toEntity")
@KonvertTo(ModDto::class)
data class Mod(
    val id: String,
    val name: String,
    val description: String? = null,
    val imagePath: String,
    val creationDate: LocalDateTime,
    val publisherId: UserId,
    val gameId: GameId,
    val categoryIds: Set<CategoryId> = emptySet(),
    val versions: MutableList<Version> = mutableListOf()
) {
  constructor() : this("", null, UserId(), GameId(""), "")

  constructor(
      name: String,
      description: String? = null,
      publisherId: UserId,
      gameId: GameId,
      imagePath: String,
      categoryIds: Set<CategoryId> = emptySet(),
      versions: List<Version> = emptyList()
  ) : this(
      id = name.spinalCase(),
      name = name,
      description = description,
      imagePath = imagePath,
      creationDate = LocalDateTime.now(),
      publisherId = publisherId,
      gameId = gameId,
      categoryIds = categoryIds,
      versions = versions.toMutableList())
}
