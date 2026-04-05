package git.walhay.modweave.api.collection

import git.walhay.modweave.api.collection.http.dto.CollectionResponseDto
import git.walhay.modweave.api.collection.repository.CollectionEntity
import git.walhay.modweave.api.collection.repository.CollectionItemEntity
import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.api.mod.repository.ModEntity
import git.walhay.modweave.api.mod.repository.fromMod
import git.walhay.modweave.api.user.UserId
import io.mcarle.konvert.api.KonvertTo
import io.mcarle.konvert.api.Mapping

@KonvertTo(
    CollectionEntity::class,
    mapFunctionName = "toEntity",
    mappings = [Mapping("mods", expression = "this.modsMapToList()")])
@KonvertTo(CollectionResponseDto::class)
data class Collection(
    val id: CollectionId,
    val name: String,
    val description: String?,
    val owner: UserId,
    val mods: MutableMap<Int, Mod> = mutableMapOf()
) {
  constructor(
      name: String,
      description: String?,
      owner: UserId,
  ) : this(CollectionId(), name, description, owner)

  fun modsMapToList(): List<CollectionItemEntity> =
      this.mods.entries
          .map { CollectionItemEntity(it.key, id, ModEntity.fromMod(it.value)) }
          .toList()
}
