package git.walhay.modweave.domain.comment

import git.walhay.modweave.domain.common.paging.Page
import git.walhay.modweave.domain.mod.ModId
import org.springframework.data.domain.Pageable

interface CommentRepository {
  fun findByModId(modId: ModId, pageable: Pageable): Page<Comment>

  fun findById(id: CommentId): Comment?

  fun save(comment: Comment): Comment

  fun delete(id: CommentId)
}
