package git.walhay.modweave.api.collection

import git.walhay.modweave.api.collection.command.CollectionCreateCommand
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.user.UserId

interface ICollectionService {
  fun getCollectionById(id: CollectionId): Collection

  fun listCollectionsByOwner(userId: UserId): List<Collection>

  fun searchCollectionsByName(name: String): List<Collection>

  fun createCollection(
      userId: UserId,
      command: CollectionCreateCommand,
  ): Collection

  fun deleteCollection(
      userId: UserId,
      collectionId: CollectionId,
  )

  fun addModToCollection(
      userId: UserId,
      collectionId: CollectionId,
      modId: ModId,
      index: Int?,
  ): Collection

  fun deleteModFromCollection(
      userId: UserId,
      collectionId: CollectionId,
      modId: ModId,
  )
}
