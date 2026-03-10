package git.walhay.modweave.api.mod

import git.walhay.modweave.api.category.Category
import git.walhay.modweave.api.game.Game
import git.walhay.modweave.api.mod.dto.ModDto
import git.walhay.modweave.api.mod.repository.ModEntity
import git.walhay.modweave.api.user.User
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
    val publisher: User,
    var game: Game,
    val categories: Set<Category> = emptySet(),
    val versions: MutableList<Version> = mutableListOf()
) {
	constructor(
        name: String,
        description: String? = null,
        publisher: User,
        game: Game,
        imagePath: String,
        categories: Set<Category> = emptySet(),
        versions: List<Version> = emptyList()
	) : this(
		id = name.spinalCase(),
		name = name,
		description = description,
		imagePath = imagePath,
		creationDate = LocalDateTime.now(),
		publisher = publisher,
		game = game,
		categories = categories,
		versions = versions.toMutableList()
	)
}