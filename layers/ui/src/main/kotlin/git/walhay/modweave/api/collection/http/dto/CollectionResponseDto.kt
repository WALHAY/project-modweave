package git.walhay.modweave.api.collection.http.dto

import git.walhay.modweave.api.collection.Collection
import java.util.UUID

data class CollectionResponseDto(
    val id: UUID,
    val name: String,
    val description: String?,
) {
  companion object {
    fun fromCollection(collection: Collection): CollectionResponseDto =
        CollectionResponseDto(
            id = collection.id.value,
            name = collection.name,
            description = collection.description,
        )
  }
}
