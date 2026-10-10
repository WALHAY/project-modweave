package git.walhay.modweave.domain.collection

import git.walhay.modweave.domain.common.paging.Page
import git.walhay.modweave.domain.user.UserId
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
