package git.walhay.modweave.api.collection.dto

import git.walhay.modweave.api.mod.Mod

data class CollectionResponseDto(val name: String, val description: String?, val mods: Map<Int, Mod>)
