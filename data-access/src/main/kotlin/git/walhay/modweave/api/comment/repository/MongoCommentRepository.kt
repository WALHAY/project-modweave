package git.walhay.modweave.api.comment.repository

import git.walhay.modweave.api.comment.Comment
import git.walhay.modweave.api.comment.CommentId
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.mongo.SequenceService
import org.springframework.context.annotation.Profile
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Document(collection = "comments")
data class CommentDocument(
    @Id val id: Long,
    val content: String,
    val publishDate: java.time.LocalDateTime,
    val authorId: String,
    val modId: String,
    val parentCommentId: Long? = null,
)

interface SpringDataMongoCommentRepository : MongoRepository<CommentDocument, Long> {
  fun findByModId(modId: String): List<CommentDocument>
}

@Repository
@Profile("mongodb")
class MongoCommentRepository(
    private val repository: SpringDataMongoCommentRepository,
    private val seq: SequenceService
) : git.walhay.modweave.api.comment.repository.CommentRepository {
  private fun CommentDocument.toDomain(): Comment =
      Comment(
          id = CommentId(this.id),
          content = this.content,
          publishDate = this.publishDate,
          authorId = git.walhay.modweave.api.user.UserId(this.authorId),
          modId = git.walhay.modweave.api.mod.ModId(this.modId),
          parentCommentId = this.parentCommentId?.let { CommentId(it) },
      )

  private fun Comment.toDocument(): CommentDocument {
    val id = if (this.id.value == 0L) seq.nextId("comment_seq") else this.id.value
    return CommentDocument(
        id,
        this.content,
        this.publishDate,
        this.authorId.value,
        this.modId.value,
        this.parentCommentId?.value)
  }

  override fun findById(id: CommentId): Comment? =
      repository.findById(id.value).map { it.toDomain() }.orElse(null)

  override fun findByModId(modId: ModId): List<Comment> =
      repository.findByModId(modId.value).map { it.toDomain() }

  override fun save(comment: Comment): Comment = repository.save(comment.toDocument()).toDomain()

  override fun delete(id: CommentId) = repository.deleteById(id.value)
}
