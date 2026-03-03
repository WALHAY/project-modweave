package git.walhay.modweave.api.mod.dto

import git.walhay.modweave.api.category.dto.CategoryDto
import git.walhay.modweave.api.game.dto.GameDto
import git.walhay.modweave.api.version.dto.VersionDto
import java.time.LocalDateTime

data class ModDto(
	val id: String,
	val name: String,
	val description: String?,
	val imagePath: String,
	val creationDate: LocalDateTime = LocalDateTime.now(),
	val game: GameDto,
	val categories: List<CategoryDto>,
	val versions: List<VersionDto>
)
