package git.walhay.modweave.api.collection

import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.api.user.UserId

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
        owner: UserId,
        mods: MutableList<Mod> = mutableListOf()
    ) : this(CollectionId(), name, description, owner, mods)
}
