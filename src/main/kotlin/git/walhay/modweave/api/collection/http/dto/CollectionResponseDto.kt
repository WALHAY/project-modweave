package git.walhay.modweave.api.collection.http.dto

import git.walhay.modweave.api.collection.Collection
import io.mcarle.konvert.api.KonvertFrom

@KonvertFrom(Collection::class)
data class CollectionResponseDto(
    val name: String,
    val description: String?,
) {
  companion object
}
