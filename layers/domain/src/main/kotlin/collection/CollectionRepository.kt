package git.walhay.modweave.domain.collection

import git.walhay.modweave.domain.common.paging.Page
import git.walhay.modweave.domain.common.paging.PageRequest
import git.walhay.modweave.domain.user.UserId

interface CollectionRepository {
  fun findById(id: CollectionId): Collection?

  fun findAllByUser(
      username: UserId,
      pageRequest: PageRequest,
  ): Page<Collection>

  fun save(collection: Collection): Collection

  fun deleteById(id: CollectionId)
}
