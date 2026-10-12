package git.walhay.modweave.persistence.comment

import git.walhay.modweave.domain.comment.Comment
import git.walhay.modweave.domain.comment.CommentId
import git.walhay.modweave.domain.comment.CommentRepository
import git.walhay.modweave.domain.common.paging.Page
import git.walhay.modweave.domain.mod.ModId
import git.walhay.modweave.persistence.common.paging.toDomainPage
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
