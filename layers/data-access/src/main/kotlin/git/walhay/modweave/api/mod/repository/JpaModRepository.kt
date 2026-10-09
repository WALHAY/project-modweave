package git.walhay.modweave.api.mod.repository

import git.walhay.modweave.api.collection.CollectionId
import git.walhay.modweave.api.common.paging.Page
import git.walhay.modweave.api.common.paging.toDomainPage
import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.user.UserId
import git.walhay.modweave.api.version.VersionStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class JpaModRepository(
    private val repository: SpringDataModRepository,
) : ModRepository {
  override fun findById(modId: ModId, visibleStatus: VersionStatus?): Mod? =
      (if (visibleStatus != null) repository.findByIdWithVersionStatus(modId.value, visibleStatus)
          else repository.findByIdOrNull(modId.value))
          ?.toDomain()

  override fun deleteById(modId: ModId) = repository.deleteById(modId.value)

  override fun save(mod: Mod): Mod = repository.save(ModEntity.fromMod(mod)).toDomain()

  override fun findAll(pageable: Pageable, visibleStatus: VersionStatus?): Page<Mod> =
      (visibleStatus?.let { repository.findAllWithVersionStatus(it, pageable) }
              ?: repository.findAll(pageable))
          .toDomainPage { it.toDomain() }

  override fun findAll(
      name: String,
      pageable: Pageable,
      visibleStatus: VersionStatus?,
  ): Page<Mod> =
      (visibleStatus?.let {
            repository.findAllByNameContainingIgnoreCaseAndVersionStatus(name, it, pageable)
          } ?: repository.findAllByNameContainingIgnoreCase(name, pageable))
          .toDomainPage { it.toDomain() }

  override fun findAllByUser(
      username: UserId,
      pageable: Pageable,
      visibleStatus: VersionStatus?,
  ): Page<Mod> =
      (visibleStatus?.let {
            repository.findAllByPublisherIdAndVersionStatus(username.value, it, pageable)
          } ?: repository.findAllByPublisherId(username.value, pageable))
          .toDomainPage { it.toDomain() }

  override fun findModsInCollection(
      collectionId: CollectionId,
      pageable: Pageable,
      visibleStatus: VersionStatus?,
  ): Page<Mod> =
      (visibleStatus?.let {
            repository.findByCollectionIdAndVersionStatus(collectionId.value, it, pageable)
          } ?: repository.findByCollectionId(collectionId.value, pageable))
          .toDomainPage { it.toDomain() }

  override fun existsById(modId: ModId): Boolean = repository.existsById(modId.value)
}
