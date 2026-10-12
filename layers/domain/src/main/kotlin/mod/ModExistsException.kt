package git.walhay.modweave.domain.mod

class ModExistsException(
    modId: ModId,
) : RuntimeException("Mod with id=\"$modId\" already exists")
