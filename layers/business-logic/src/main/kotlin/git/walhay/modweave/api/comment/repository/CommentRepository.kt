package git.walhay.modweave.api.comment.repository

import git.walhay.modweave.api.comment.Comment
import git.walhay.modweave.api.comment.CommentId
import git.walhay.modweave.api.common.paging.Page
import git.walhay.modweave.api.mod.ModId
import org.springframework.data.domain.Pageable

interface CommentRepository {
  fun findByModId(modId: ModId, pageable: Pageable): Page<Comment>

  fun findById(id: CommentId): Comment?

  fun save(comment: Comment): Comment

  fun delete(id: CommentId)
}
