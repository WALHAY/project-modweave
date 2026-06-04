package git.walhay.modweave.api.collection.repository

import jakarta.persistence.Embeddable
import java.util.UUID

@Embeddable
data class CollectionItemId(val index: Int = 0, val collectionId: UUID = UUID.randomUUID())
