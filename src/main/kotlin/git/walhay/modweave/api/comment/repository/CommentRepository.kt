package git.walhay.modweave.api.comment.repository

import git.walhay.modweave.api.comment.Comment
import git.walhay.modweave.api.comment.CommentId

interface CommentRepository {
  fun findById(id: CommentId): Comment?

  fun save(comment: Comment): Comment

  fun delete(id: CommentId)
}
