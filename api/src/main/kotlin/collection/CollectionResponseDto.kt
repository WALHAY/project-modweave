package git.walhay.modweave.api.collection

import com.fasterxml.jackson.annotation.JsonIgnore
import git.walhay.modweave.api.common.ApiLink
import git.walhay.modweave.api.common.ApiLinkContext
import git.walhay.modweave.api.common.ApiResource
import git.walhay.modweave.domain.collection.Collection
import java.util.UUID

data class CollectionResponseDto(
    val id: UUID,
    val name: String,
    val description: String?,
    @get:JsonIgnore val ownerId: String,
    override val links: Map<String, ApiLink> = emptyMap(),
) : ApiResource {
  override fun withLinks(context: ApiLinkContext): CollectionResponseDto =
      with(context) {
        copy(
            links =
                buildMap {
                  put("self", link("/collections/{id}", id))
                  put("mods", collection("/collections/{id}/mods", id))
                  if (canManage(ownerId)) {
                    put("add-mod", link("/collections/{id}/mods", id, method = "POST"))
                    put("delete", link("/collections/{id}", id, method = "DELETE"))
                  }
                })
      }

  companion object {
    fun fromCollection(collection: Collection): CollectionResponseDto =
        CollectionResponseDto(
            id = collection.id.value,
            name = collection.name,
            description = collection.description,
            ownerId = collection.owner.value,
        )
  }
}
