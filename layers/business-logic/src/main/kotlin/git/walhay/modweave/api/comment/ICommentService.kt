package git.walhay.modweave.api.comment

import git.walhay.modweave.api.comment.command.CommentCreateCommand
import git.walhay.modweave.api.common.paging.Page
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.user.UserId

interface ICommentService {
  fun findCommentsByMod(modId: ModId, page: Int, size: Int): Page<Comment>

  fun findCommentById(id: CommentId): Comment?

  fun createComment(
      userId: UserId,
      command: CommentCreateCommand,
  ): Comment

  fun deleteComment(
      userId: UserId,
      id: CommentId,
  )
}
