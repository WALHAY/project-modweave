package git.walhay.modweave.api.mod.command

import git.walhay.modweave.api.category.CategoryId
import git.walhay.modweave.api.game.GameId
import git.walhay.modweave.api.mod.ModId
import org.springframework.web.multipart.MultipartFile

data class ModCreateCommand(
    val modId: ModId,
    val name: String,
    val description: String,
    val image: MultipartFile,
    val categories: Set<CategoryId> = mutableSetOf(),
    val versionName: String,
    val files: List<MultipartFile> = mutableListOf(),
    val gameId: GameId
)
