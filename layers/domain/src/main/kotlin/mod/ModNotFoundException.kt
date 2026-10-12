package git.walhay.modweave.domain.mod

class ModNotFoundException(
    modId: ModId,
) : RuntimeException("Mod with id=\"$modId\" not found")
