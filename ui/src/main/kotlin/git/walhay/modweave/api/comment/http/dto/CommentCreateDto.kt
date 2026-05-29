package git.walhay.modweave.api.comment.http.dto

import git.walhay.modweave.api.comment.command.CommentCreateCommand
import git.walhay.modweave.api.comment.CommentId
import git.walhay.modweave.api.mod.ModId

data class CommentCreateDto(
    val content: String,
    val modId: String,
    val parentCommentId: Long? = null,
) {
  fun toCommentCreateCommand(): CommentCreateCommand =
      CommentCreateCommand(
          content = content,
          modId = ModId(modId),
          parentCommentId = parentCommentId?.let(::CommentId),
      )
}
