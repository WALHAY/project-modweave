package git.walhay.modweave.persistence.collection

import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataCollectionRepository : JpaRepository<CollectionEntity, UUID> {
  fun findAllByOwner(owner: String, pageable: Pageable): Page<CollectionEntity>
}
