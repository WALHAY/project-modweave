package git.walhay.modweave.api.comment.repository

import git.walhay.modweave.api.comment.Comment
import git.walhay.modweave.api.comment.CommentId
import git.walhay.modweave.api.common.paging.Page
import git.walhay.modweave.api.common.paging.toDomainPage
import git.walhay.modweave.api.mod.ModId
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class JpaCommentRepository(
    private val repository: SpringDataCommentRepository,
) : CommentRepository {
  override fun findByModId(modId: ModId, pageable: Pageable): Page<Comment> =
      repository.findAllByModId(modId.value, pageable).toDomainPage { it.toDomain() }

  override fun findById(id: CommentId): Comment? = repository.findByIdOrNull(id.value)?.toDomain()

  override fun save(comment: Comment): Comment =
      repository.save(CommentEntity.fromComment(comment)).toDomain()

  override fun delete(id: CommentId) = repository.deleteById(id.value)
}
