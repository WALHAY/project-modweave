package git.walhay.modweave.api.comment.repository

import git.walhay.modweave.api.comment.Comment
import git.walhay.modweave.api.comment.CommentId
import git.walhay.modweave.api.mod.ModId

interface CommentRepository {
  fun findById(id: CommentId): Comment?

  fun findByModId(modId: ModId): List<Comment>

  fun save(comment: Comment): Comment

  fun delete(id: CommentId)
}
