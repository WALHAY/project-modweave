package git.walhay.modweave.api.mod

import git.walhay.modweave.api.category.CategoryId
import git.walhay.modweave.api.game.GameId
import git.walhay.modweave.api.mod.dto.ModResponseDto
import git.walhay.modweave.api.mod.repository.ModEntity
import git.walhay.modweave.api.user.UserId
import git.walhay.modweave.api.version.Version
import git.walhay.modweave.util.spinalCase
import io.mcarle.konvert.api.KonvertTo
import java.time.LocalDateTime

@KonvertTo(ModEntity::class, mapFunctionName = "toEntity")
@KonvertTo(ModResponseDto::class)
data class Mod(
    val id: ModId,
    val name: String,
    val description: String? = null,
    val imagePath: String,
    val creationDate: LocalDateTime,
    val publisherId: UserId,
    val gameId: GameId,
    val categories: Set<CategoryId> = emptySet(),
    val versions: MutableList<Version> = mutableListOf()
) {
  constructor(
      name: String,
      description: String? = null,
      publisherId: UserId,
      gameId: GameId,
      imagePath: String,
      categories: Set<CategoryId> = emptySet(),
      versions: List<Version> = emptyList()
  ) : this(
      id = ModId(name.spinalCase()),
      name = name,
      description = description,
      imagePath = imagePath,
      creationDate = LocalDateTime.now(),
      publisherId = publisherId,
      gameId = gameId,
      categories = categories,
      versions = versions.toMutableList())
}
