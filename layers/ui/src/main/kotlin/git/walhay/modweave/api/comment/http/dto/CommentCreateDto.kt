package git.walhay.modweave.api.comment.http.dto

import git.walhay.modweave.api.comment.command.CommentCreateCommand
import git.walhay.modweave.api.mod.ModId

data class CommentCreateDto(
    val content: String,
    val modId: String,
) {
  fun toCommentCreateCommand(): CommentCreateCommand = CommentCreateCommand(content, ModId(modId))
}
