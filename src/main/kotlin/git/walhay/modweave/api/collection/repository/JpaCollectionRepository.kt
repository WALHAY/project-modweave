package git.walhay.modweave.api.collection.repository

import git.walhay.modweave.api.collection.Collection
import git.walhay.modweave.api.collection.CollectionId
import git.walhay.modweave.api.collection.toEntity
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class JpaCollectionRepository(private val repository: SpringDataCollectionRepository) :
    CollectionRepository {
  override fun findById(id: CollectionId): Collection? =
      repository.findByIdOrNull(id)?.toModel()

  override fun save(collection: Collection): Collection =
      repository.save(collection.toEntity()).toModel()
}
