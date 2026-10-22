package git.walhay.modweave.persistence.mod

import git.walhay.modweave.domain.collection.CollectionId
import git.walhay.modweave.domain.common.paging.Page
import git.walhay.modweave.domain.common.paging.PageRequest
import git.walhay.modweave.domain.mod.Mod
import git.walhay.modweave.domain.mod.ModId
import git.walhay.modweave.domain.mod.ModRepository
import git.walhay.modweave.domain.user.UserId
import git.walhay.modweave.domain.version.VersionStatus
import git.walhay.modweave.persistence.common.paging.toDomainPage
import git.walhay.modweave.persistence.common.paging.toSpringPageable
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

  override fun findAll(pageRequest: PageRequest, visibleStatus: VersionStatus?): Page<Mod> =
      (visibleStatus?.let {
            repository.findAllWithVersionStatus(it, pageRequest.toSpringPageable())
          } ?: repository.findAll(pageRequest.toSpringPageable()))
          .toDomainPage { it.toDomain() }

  override fun findAll(
      name: String,
      pageRequest: PageRequest,
      visibleStatus: VersionStatus?,
  ): Page<Mod> =
      (visibleStatus?.let {
            repository.findAllByNameContainingIgnoreCaseAndVersionStatus(
                name, it, pageRequest.toSpringPageable())
          } ?: repository.findAllByNameContainingIgnoreCase(name, pageRequest.toSpringPageable()))
          .toDomainPage { it.toDomain() }

  override fun findAllByUser(
      username: UserId,
      pageRequest: PageRequest,
      visibleStatus: VersionStatus?,
  ): Page<Mod> =
      (visibleStatus?.let {
            repository.findAllByPublisherIdAndVersionStatus(
                username.value, it, pageRequest.toSpringPageable())
          } ?: repository.findAllByPublisherId(username.value, pageRequest.toSpringPageable()))
          .toDomainPage { it.toDomain() }

  override fun findModsInCollection(
      collectionId: CollectionId,
      pageRequest: PageRequest,
      visibleStatus: VersionStatus?,
  ): Page<Mod> =
      (visibleStatus?.let {
            repository.findByCollectionIdAndVersionStatus(
                collectionId.value, it, pageRequest.toSpringPageable())
          } ?: repository.findByCollectionId(collectionId.value, pageRequest.toSpringPageable()))
          .toDomainPage { it.toDomain() }

  override fun existsById(modId: ModId): Boolean = repository.existsById(modId.value)
}
