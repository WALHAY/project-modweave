package git.walhay.modweave.api.collection.http.dto

import git.walhay.modweave.api.collection.Collection

data class CollectionResponseDto(
    val name: String,
    val description: String?,
) {
  companion object {
    fun fromCollection(collection: Collection): CollectionResponseDto =
        CollectionResponseDto(
            name = collection.name,
            description = collection.description,
        )
  }
}
