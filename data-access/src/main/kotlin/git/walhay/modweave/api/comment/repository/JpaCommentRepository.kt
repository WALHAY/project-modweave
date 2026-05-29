package git.walhay.modweave.api.comment.repository

import git.walhay.modweave.api.comment.Comment
import git.walhay.modweave.api.comment.CommentId
import git.walhay.modweave.api.mod.ModId
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
@org.springframework.context.annotation.Profile("postgres")
class JpaCommentRepository(
    private val repository: SpringDataCommentRepository,
) : CommentRepository {
  override fun findById(id: CommentId): Comment? = repository.findByIdOrNull(id.value)?.toDomain()

  override fun findByModId(modId: ModId): List<Comment> =
      repository.findByModIdOrderByPublishDateAscIdAsc(modId.value).map { it.toDomain() }

  override fun save(comment: Comment): Comment =
      repository.save(CommentEntity.fromComment(comment)).toDomain()

  override fun delete(id: CommentId) = repository.deleteById(id.value)
}
