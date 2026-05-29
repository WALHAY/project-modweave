package git.walhay.modweave.api.mod.http.dto

import git.walhay.modweave.api.mod.Mod
import java.time.LocalDateTime

data class ModResponseDto(
    val id: String,
    val name: String,
    val description: String?,
    val imagePath: String,
    val creationDate: LocalDateTime = LocalDateTime.now(),
    val publisherId: String,
    val gameId: String,
    val categories: List<String>,
) {
  companion object {
    fun fromMod(mod: Mod): ModResponseDto =
        ModResponseDto(
            id = mod.id.value,
            name = mod.name,
            description = mod.description,
            imagePath = mod.imagePath,
            creationDate = mod.creationDate,
            publisherId = mod.publisherId.value,
            gameId = mod.gameId.value,
            categories = mod.categories.map { it.value }.toList(),
        )
  }
}
