package git.walhay.modweave.api.collection

import git.walhay.modweave.api.collection.command.CollectionCreateCommand
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.user.UserId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort

interface ICollectionService {
  fun getCollectionById(id: CollectionId): Collection

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

  fun findCollectionsOfUser(
      id: UserId,
      page: Int,
      size: Int,
      sort: Sort,
  ): Page<Collection>

  fun deleteModFromCollection(
      userId: UserId,
      collectionId: CollectionId,
      modId: ModId,
  )
}
