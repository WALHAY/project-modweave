package git.walhay.modweave.api.collection.repository

import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataCollectionRepository : JpaRepository<CollectionEntity, Long> {
  fun findByOwner(owner: String): List<CollectionEntity>

  fun findByNameContainingIgnoreCase(name: String): List<CollectionEntity>
}
