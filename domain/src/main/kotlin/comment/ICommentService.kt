package git.walhay.modweave.domain.comment

import git.walhay.modweave.domain.common.paging.Page
import git.walhay.modweave.domain.mod.ModId
import git.walhay.modweave.domain.user.UserId

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
