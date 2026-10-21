package git.walhay.modweave.api.mod

import git.walhay.modweave.api.common.ApiResource
import git.walhay.modweave.api.common.apiLink
import git.walhay.modweave.api.image.ImageController
import git.walhay.modweave.domain.mod.Mod
import java.time.LocalDateTime

data class ModResponseDto(
    val id: String,
    val name: String,
    val description: String?,
    val imagePath: String,
    val creationDate: LocalDateTime,
    val publisherId: String,
    val gameId: String,
    val categories: List<String>,
) : ApiResource {
  companion object {
    fun fromMod(mod: Mod): ModResponseDto =
        ModResponseDto(
            id = mod.id.value,
            name = mod.name,
            description = mod.description,
            imagePath = apiLink(ImageController::getModImage, mod.id.value).href,
            creationDate = mod.creationDate,
            publisherId = mod.publisherId.value,
            gameId = mod.gameId.value,
            categories = mod.categories.map { it.toString() }.toList(),
        )
  }
}
