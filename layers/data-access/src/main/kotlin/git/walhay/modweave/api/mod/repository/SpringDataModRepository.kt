package git.walhay.modweave.api.mod.repository

import git.walhay.modweave.api.version.VersionStatus
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface SpringDataModRepository : JpaRepository<ModEntity, String> {
  @Query(
      """
        SELECT m FROM ModEntity m
        WHERE m.id = :id
          AND EXISTS (
            SELECT v.id FROM VersionEntity v
            WHERE v.modId = m.id AND v.status = :status
          )
      """,
  )
  fun findByIdWithVersionStatus(
      @Param("id") id: String,
      @Param("status") status: VersionStatus,
  ): ModEntity?

  @Query(
      """
        SELECT m FROM ModEntity m
        WHERE EXISTS (
            SELECT v.id FROM VersionEntity v
            WHERE v.modId = m.id AND v.status = :status
        )
      """,
  )
  fun findAllWithVersionStatus(
      @Param("status") status: VersionStatus,
      pageable: Pageable,
  ): Page<ModEntity>

  @Query(
      """
        SELECT m FROM ModEntity m
        WHERE lower(m.name) LIKE lower(concat('%', :name, '%'))
          AND EXISTS (
            SELECT v.id FROM VersionEntity v
            WHERE v.modId = m.id AND v.status = :status
          )
      """,
  )
  fun findAllByNameContainingIgnoreCaseAndVersionStatus(
      @Param("name") name: String,
      @Param("status") status: VersionStatus,
      pageable: Pageable,
  ): Page<ModEntity>

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
        SELECT m FROM ModEntity m
        WHERE m.publisherId = :publisherId
          AND EXISTS (
            SELECT v.id FROM VersionEntity v
            WHERE v.modId = m.id AND v.status = :status
          )
      """,
  )
  fun findAllByPublisherIdAndVersionStatus(
      @Param("publisherId") publisherId: String,
      @Param("status") status: VersionStatus,
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

  @Query(
      """
        SELECT ci.mod
        FROM CollectionItemEntity ci
        JOIN ci.mod m
        WHERE ci.collectionId = :id
          AND EXISTS (
            SELECT v.id FROM VersionEntity v
            WHERE v.modId = m.id AND v.status = :status
          )
      """,
  )
  fun findByCollectionIdAndVersionStatus(
      @Param("id") id: UUID,
      @Param("status") status: VersionStatus,
      pageable: Pageable,
  ): Page<ModEntity>
}
