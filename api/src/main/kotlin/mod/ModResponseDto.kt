package git.walhay.modweave.api.mod

import git.walhay.modweave.api.common.ApiLink
import git.walhay.modweave.api.common.ApiLinkContext
import git.walhay.modweave.api.common.ApiResource
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
    override val links: Map<String, ApiLink> = emptyMap(),
) : ApiResource {
  override fun withLinks(context: ApiLinkContext): ModResponseDto =
      with(context) {
        copy(
            links =
                buildMap {
                  put("self", link("/mods/{id}", id))
                  put("game", link("/games/{id}", gameId))
                  put("publisher", link("/users/{id}", publisherId))
                  put("versions", collection("/mods/{id}/versions", id))
                  put("comments", collection("/comments", query = mapOf("modId" to id)))
                  if (canManage(publisherId)) {
                    put("create-version", link("/mods/{id}/versions", id, method = "POST"))
                    put("delete", link("/mods/{id}", id, method = "DELETE"))
                  }
                })
      }

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
            categories = mod.categories.map { it.toString() }.toList(),
        )
  }
}
