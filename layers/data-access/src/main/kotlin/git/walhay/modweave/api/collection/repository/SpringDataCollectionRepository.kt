package git.walhay.modweave.api.collection.repository

import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataCollectionRepository : JpaRepository<CollectionEntity, UUID>
