package git.walhay.modweave.api.collection.repository

import git.walhay.modweave.api.collection.Collection
import git.walhay.modweave.api.collection.CollectionId
import git.walhay.modweave.api.mod.repository.ModEntity
import git.walhay.modweave.api.user.UserId
import jakarta.persistence.EntityManager
import jakarta.persistence.LockModeType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
@Transactional
class JpaCollectionRepository(
    private val repository: SpringDataCollectionRepository,
    private val entityManager: EntityManager,
) : CollectionRepository {
  override fun findById(id: CollectionId): Collection? =
      repository.findByIdOrNull(id.value)?.toDomain()

  override fun findAllByUser(username: UserId, pageable: Pageable): Page<Collection> =
      repository.findAllByOwner(username.value, pageable).map { it.toDomain() }

  override fun save(collection: Collection): Collection {
    // Flush removal before insertion so reordering cannot violate the two unique keys.
    entityManager.find(
        CollectionEntity::class.java, collection.id.value, LockModeType.PESSIMISTIC_WRITE)
    repository.saveAndFlush(
        CollectionEntity(
            collection.id.value, collection.name, collection.description, collection.owner.value))
    entityManager
        .createQuery("delete from CollectionItemEntity ci where ci.collectionId = :id")
        .setParameter("id", collection.id.value)
        .executeUpdate()
    entityManager.clear()
    collection.mods.forEachIndexed { index, mod ->
      entityManager.persist(
          CollectionItemEntity(
              index,
              collection.id.value,
              entityManager.getReference(ModEntity::class.java, mod.id.value)))
    }
    entityManager.flush()
    val entity = entityManager.find(CollectionEntity::class.java, collection.id.value)
    entityManager.refresh(entity)
    return entity.toDomain()
  }

  override fun deleteById(id: CollectionId) = repository.deleteById(id.value)
}
