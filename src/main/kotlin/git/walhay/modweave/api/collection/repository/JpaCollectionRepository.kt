package git.walhay.modweave.api.collection.repository

import git.walhay.modweave.api.collection.Collection
import git.walhay.modweave.api.collection.CollectionId
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class JpaCollectionRepository(private val repository: SpringDataCollectionRepository) :
    CollectionRepository {
  override fun findById(id: CollectionId): Collection? =
      repository.findByIdOrNull(id.value)?.toDomain()

  override fun save(collection: Collection): Collection =
      repository.save(CollectionEntity.fromCollection(collection)).toDomain()
}
