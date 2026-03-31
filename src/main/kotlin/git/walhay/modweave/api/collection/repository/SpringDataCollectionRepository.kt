package git.walhay.modweave.api.collection.repository

import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataCollectionRepository : JpaRepository<CollectionEntity, Long> {}
