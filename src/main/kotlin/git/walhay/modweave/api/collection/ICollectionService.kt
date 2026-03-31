package git.walhay.modweave.api.collection

import git.walhay.modweave.api.collection.dto.CollectionCreateDto
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.user.UserId

interface ICollectionService {
  fun getCollectionById(id: CollectionId): Collection

  fun createCollection(username: UserId, dto: CollectionCreateDto): Collection

  fun addModToCollection(username: UserId, collectionId: CollectionId, modId: ModId): Collection

  fun removeModFromCollection(username: UserId, collectionId: CollectionId, modId: ModId)
}
