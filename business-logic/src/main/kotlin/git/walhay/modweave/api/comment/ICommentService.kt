package git.walhay.modweave.api.comment

import git.walhay.modweave.api.comment.command.CommentCreateCommand
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.user.UserId

interface ICommentService {
  fun findCommentById(id: CommentId): Comment?

  fun findCommentsByModId(modId: ModId): List<Comment>

  fun createComment(
      userId: UserId,
      command: CommentCreateCommand,
  ): Comment

  fun deleteComment(
      userId: UserId,
      id: CommentId,
  )
}
