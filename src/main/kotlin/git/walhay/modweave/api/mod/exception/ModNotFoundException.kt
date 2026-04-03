package git.walhay.modweave.api.mod.exception

import git.walhay.modweave.api.mod.ModId

class ModNotFoundException(modId: ModId) : Exception("Mod with id=\"$modId\" not found")
