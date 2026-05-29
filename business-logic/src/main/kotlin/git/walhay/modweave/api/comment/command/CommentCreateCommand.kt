package git.walhay.modweave.api.comment.command

import git.walhay.modweave.api.comment.CommentId
import git.walhay.modweave.api.mod.ModId

data class CommentCreateCommand(
    val content: String,
    val modId: ModId,
    val parentCommentId: CommentId? = null,
)
