package git.walhay.modweave.domain.comment

import git.walhay.modweave.domain.common.paging.Page
import git.walhay.modweave.domain.common.paging.PageRequest
import git.walhay.modweave.domain.mod.ModId

interface CommentRepository {
  fun findByModId(modId: ModId, pageRequest: PageRequest): Page<Comment>

  fun findById(id: CommentId): Comment?

  fun save(comment: Comment): Comment

  fun delete(id: CommentId)
}
