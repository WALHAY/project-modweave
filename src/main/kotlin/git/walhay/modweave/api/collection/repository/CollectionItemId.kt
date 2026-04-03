package git.walhay.modweave.api.collection.repository

import jakarta.persistence.Embeddable

@Embeddable
data class CollectionItemId(
    val index: Int = 0,
    val collectionId: Long = 0
)