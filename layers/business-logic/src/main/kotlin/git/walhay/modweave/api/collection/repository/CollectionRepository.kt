package git.walhay.modweave.api.collection.repository

import git.walhay.modweave.api.collection.Collection
import git.walhay.modweave.api.collection.CollectionId
import git.walhay.modweave.api.user.UserId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface CollectionRepository {
  fun findById(id: CollectionId): Collection?

  fun findAllByUser(
      username: UserId,
      pageable: Pageable,
  ): Page<Collection>

  fun save(collection: Collection): Collection

  fun deleteById(id: CollectionId)
}
