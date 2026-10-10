package git.walhay.modweave.api.game

import git.walhay.modweave.api.common.ApiLink
import git.walhay.modweave.api.common.ApiLinkContext
import git.walhay.modweave.api.common.ApiResource
import git.walhay.modweave.domain.game.Game

data class GameResponseDto(
    val id: String,
    val name: String,
    val description: String?,
    val imagePath: String,
    override val links: Map<String, ApiLink> = emptyMap(),
) : ApiResource {
  override fun withLinks(context: ApiLinkContext): GameResponseDto =
      with(context) {
        copy(
            links =
                buildMap {
                  put("self", link("/games/{id}", id))
                  put("mods", collection("/mods"))
                  if (isAdmin) put("delete", link("/games/{id}", id, method = "DELETE"))
                })
      }

  companion object {
    fun fromGame(game: Game): GameResponseDto =
        GameResponseDto(
            id = game.id.value,
            name = game.name,
            description = game.description,
            imagePath = game.imagePath,
        )
  }
}
