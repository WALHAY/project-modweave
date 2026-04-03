package git.walhay.modweave.api.collection

import git.walhay.modweave.api.collection.dto.CollectionCreateDto
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.user.UserId

interface ICollectionService {
  fun getCollectionById(id: CollectionId): Collection

  fun createCollection(userId: UserId, dto: CollectionCreateDto): Collection

  fun addModToCollection(
      userId: UserId,
      collectionId: CollectionId,
      modId: ModId,
      index: Int?
  ): Collection

  fun removeModFromCollection(userId: UserId, collectionId: CollectionId, modId: ModId)
}
