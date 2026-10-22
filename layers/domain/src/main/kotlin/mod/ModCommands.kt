package git.walhay.modweave.domain.mod

import git.walhay.modweave.domain.category.CategoryId
import git.walhay.modweave.domain.game.GameId
import org.springframework.web.multipart.MultipartFile

data class ModCreateCommand(
    val id: ModId,
    val name: String,
    val description: String?,
    val image: MultipartFile,
    val categories: Set<CategoryId> = emptySet(),
    val versionName: String,
    val files: List<MultipartFile> = emptyList(),
    val gameId: GameId,
)

class ModExistsException(
    modId: ModId,
) : RuntimeException("Mod with id=\"$modId\" already exists")

class ModNotFoundException(
    modId: ModId,
) : RuntimeException("Mod with id=\"$modId\" not found")
