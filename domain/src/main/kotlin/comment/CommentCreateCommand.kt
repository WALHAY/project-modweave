package git.walhay.modweave.domain.comment

import git.walhay.modweave.domain.mod.ModId

data class CommentCreateCommand(
    val content: String,
    val modId: ModId,
)
