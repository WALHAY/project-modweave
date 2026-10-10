package git.walhay.modweave.domain.collection

import git.walhay.modweave.domain.mod.Mod
import git.walhay.modweave.domain.user.UserId
import java.io.Serializable

data class Collection(
    val id: CollectionId,
    val name: String,
    val description: String?,
    val owner: UserId,
    val mods: MutableList<Mod> = mutableListOf(),
) : Serializable {
  constructor(
      name: String,
      description: String?,
      owner: UserId,
      mods: MutableList<Mod> = mutableListOf(),
  ) : this(CollectionId(), name, description, owner, mods)
}
