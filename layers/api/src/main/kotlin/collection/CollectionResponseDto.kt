package git.walhay.modweave.api.collection

import com.fasterxml.jackson.annotation.JsonIgnore
import git.walhay.modweave.api.common.ApiResource
import git.walhay.modweave.domain.collection.Collection
import java.util.UUID

data class CollectionResponseDto(
    val id: UUID,
    val name: String,
    val description: String?,
    @get:JsonIgnore val ownerId: String,
) : ApiResource {
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
