package git.walhay.modweave.api.collection.repository

import git.walhay.modweave.api.collection.Collection
import git.walhay.modweave.api.collection.CollectionId

interface CollectionRepository {
  fun findById(id: CollectionId): Collection?

  fun save(collection: Collection): Collection
}
