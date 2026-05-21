package git.walhay.modweave.api.mod

import git.walhay.modweave.api.collection.CollectionId
import git.walhay.modweave.api.mod.command.ModCreateCommand
import git.walhay.modweave.api.user.UserId
import org.springframework.data.domain.Page
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
