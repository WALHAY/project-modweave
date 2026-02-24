package git.walhay.modweave.dto

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
