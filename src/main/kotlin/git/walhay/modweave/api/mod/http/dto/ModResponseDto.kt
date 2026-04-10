package git.walhay.modweave.api.mod.http.dto

import git.walhay.modweave.api.category.CategoryId
import git.walhay.modweave.api.game.GameId
import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.user.UserId
import io.mcarle.konvert.api.KonvertFrom
import java.time.LocalDateTime

@KonvertFrom(Mod::class)
data class ModResponseDto(
    val id: ModId,
    val name: String,
    val description: String?,
    val imagePath: String,
    val creationDate: LocalDateTime = LocalDateTime.now(),
    val publisherId: UserId,
    val gameId: GameId,
    val categories: List<CategoryId>,
) {
  companion object
}
