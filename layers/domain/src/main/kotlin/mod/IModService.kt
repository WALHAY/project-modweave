package git.walhay.modweave.domain.mod

import git.walhay.modweave.domain.collection.CollectionId
import git.walhay.modweave.domain.common.paging.Page
import git.walhay.modweave.domain.user.UserId
import org.springframework.data.domain.Sort

interface IModService {
  fun findModById(modId: ModId): Mod

  fun findModsWithFilter(
      page: Int,
      size: Int,
      name: String?,
      sort: Sort,
  ): Page<Mod>

  fun findModsOfUser(
      id: UserId,
      page: Int,
      size: Int,
      sort: Sort,
  ): Page<Mod>

  fun findModsInCollection(
      id: CollectionId,
      page: Int,
      size: Int,
      sort: Sort,
  ): Page<Mod>

  fun uploadMod(
      userId: UserId,
      command: ModCreateCommand,
  ): Mod

  fun deleteMod(
      userId: UserId,
      modId: ModId,
  )
}
