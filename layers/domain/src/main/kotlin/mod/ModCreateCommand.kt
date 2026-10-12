package git.walhay.modweave.domain.mod

import git.walhay.modweave.domain.category.CategoryId
import git.walhay.modweave.domain.game.GameId
import org.springframework.web.multipart.MultipartFile

data class ModCreateCommand(
    val id: ModId,
    val name: String,
    val description: String?,
    val image: MultipartFile,
    val categories: Set<CategoryId> = mutableSetOf(),
    val versionName: String,
    val files: List<MultipartFile> = mutableListOf(),
    val gameId: GameId,
)
