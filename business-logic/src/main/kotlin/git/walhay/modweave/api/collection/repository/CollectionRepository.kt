package git.walhay.modweave.api.collection.repository

import git.walhay.modweave.api.collection.Collection
import git.walhay.modweave.api.collection.CollectionId
import git.walhay.modweave.api.user.UserId

interface CollectionRepository {
  fun findById(id: CollectionId): Collection?

  fun findByOwner(owner: UserId): List<Collection>

  fun findByNameContainingIgnoreCase(name: String): List<Collection>

  fun save(collection: Collection): Collection

  fun deleteById(id: CollectionId)
}
