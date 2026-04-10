package git.walhay.modweave.api.mod.exception

import git.walhay.modweave.api.mod.ModId

class ModExistsException(modId: ModId) : Exception("Mod with id=\"$modId\" already exists")
