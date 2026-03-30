package git.walhay.modweave.api.mod.dto

import git.walhay.modweave.api.category.CategoryId
import git.walhay.modweave.api.game.GameId
import git.walhay.modweave.api.user.UserId
import java.time.LocalDateTime

data class ModDto(
    val id: String,
    val name: String,
    val description: String?,
    val imagePath: String,
    val creationDate: LocalDateTime = LocalDateTime.now(),
    val publisherId: UserId,
    val gameId: GameId,
    val categoryIds: List<CategoryId>,
)
