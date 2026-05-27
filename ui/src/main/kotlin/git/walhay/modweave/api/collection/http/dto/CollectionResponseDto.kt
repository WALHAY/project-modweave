package git.walhay.modweave.api.collection.http.dto

import git.walhay.modweave.api.collection.Collection

data class CollectionResponseDto(
    val id: Long,
    val name: String,
    val description: String?,
    val ownerId: String,
) {
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
