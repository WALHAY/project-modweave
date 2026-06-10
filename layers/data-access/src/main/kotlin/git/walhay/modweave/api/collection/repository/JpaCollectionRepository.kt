package git.walhay.modweave.api.collection.repository

import git.walhay.modweave.api.collection.Collection
import git.walhay.modweave.api.collection.CollectionId
import git.walhay.modweave.api.user.UserId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class JpaCollectionRepository(
    private val repository: SpringDataCollectionRepository,
) : CollectionRepository {
  override fun findById(id: CollectionId): Collection? =
      repository.findByIdOrNull(id.value)?.toDomain()

  override fun findAllByUser(username: UserId, pageable: Pageable): Page<Collection> =
      repository.findAllByOwner(username.value, pageable).map { it.toDomain() }

  override fun save(collection: Collection): Collection =
      repository.save(CollectionEntity.fromCollection(collection)).toDomain()

  override fun deleteById(id: CollectionId) = repository.deleteById(id.value)
}
