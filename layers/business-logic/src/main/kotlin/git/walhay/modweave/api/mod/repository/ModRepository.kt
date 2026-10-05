package git.walhay.modweave.api.mod.repository

import git.walhay.modweave.api.collection.CollectionId
import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.user.UserId
import git.walhay.modweave.api.version.VersionStatus
import org.springframework.data.domain.Page
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
