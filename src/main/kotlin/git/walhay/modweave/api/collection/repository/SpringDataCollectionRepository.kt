package git.walhay.modweave.api.collection.repository

import git.walhay.modweave.api.collection.CollectionId
import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataCollectionRepository : JpaRepository<CollectionEntity, CollectionId>
