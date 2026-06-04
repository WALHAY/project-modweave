package git.walhay.modweave.api.mod.repository

import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface SpringDataModRepository : JpaRepository<ModEntity, String> {
  fun findAllByNameContainingIgnoreCase(
      name: String,
      pageable: Pageable,
  ): Page<ModEntity>

  fun findAllByPublisherId(
      id: String,
      pageable: Pageable,
  ): Page<ModEntity>

  @Query(
      """
        SELECT ci.mod 
        FROM CollectionItemEntity ci 
        JOIN ci.mod m 
        WHERE ci.collectionId = :id 
    """,
  )
  fun findByCollectionId(
      @Param("id") id: UUID,
      pageable: Pageable,
  ): Page<ModEntity>
}
