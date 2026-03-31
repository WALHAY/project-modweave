package git.walhay.modweave.api.collection

import git.walhay.modweave.api.collection.dto.CollectionResponseDto
import git.walhay.modweave.api.collection.repository.CollectionEntity
import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.api.user.UserId
import io.mcarle.konvert.api.KonvertTo

@KonvertTo(CollectionEntity::class, mapFunctionName = "toEntity")
@KonvertTo(CollectionResponseDto::class)
data class Collection(
    val id: CollectionId,
    val name: String,
    val description: String?,
    val owner: UserId,
    val mods: MutableList<Mod> = mutableListOf()
) {
  constructor(
      name: String,
      description: String?,
      owner: UserId
  ) : this(CollectionId(), name, description, owner)
}
