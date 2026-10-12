package git.walhay.modweave.domain.mod

import git.walhay.modweave.domain.collection.CollectionId
import git.walhay.modweave.domain.common.paging.Page
import git.walhay.modweave.domain.user.UserId
import git.walhay.modweave.domain.version.VersionStatus
import org.springframework.data.domain.Pageable

interface ModRepository {
  fun findById(modId: ModId, visibleStatus: VersionStatus? = null): Mod?

  fun findAll(pageable: Pageable, visibleStatus: VersionStatus? = null): Page<Mod>

  fun findAll(
      name: String,
      pageable: Pageable,
      visibleStatus: VersionStatus? = null,
  ): Page<Mod>

  fun findAllByUser(
      username: UserId,
      pageable: Pageable,
      visibleStatus: VersionStatus? = null,
  ): Page<Mod>

  fun findModsInCollection(
      collectionId: CollectionId,
      pageable: Pageable,
      visibleStatus: VersionStatus? = null,
  ): Page<Mod>

  fun existsById(modId: ModId): Boolean

  fun save(mod: Mod): Mod

  fun deleteById(modId: ModId)
}
