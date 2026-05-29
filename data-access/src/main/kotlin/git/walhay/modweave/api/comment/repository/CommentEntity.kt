package git.walhay.modweave.api.comment.repository

import git.walhay.modweave.api.comment.Comment
import git.walhay.modweave.api.comment.CommentId
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.user.UserId
import jakarta.persistence.*
import java.io.Serializable
import java.time.LocalDateTime

@Entity
@Table(schema = "modweave", name = "comments")
class CommentEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column("id") val id: Long = 0,
    @Column("content", nullable = false) val content: String = "",
    @Column("publish_date", nullable = false) val publishDate: LocalDateTime = LocalDateTime.now(),
    @Column("user_id", nullable = false) val authorId: String = "",
    @Column("mod_id", nullable = false) val modId: String = "",
    @Column("parent_comment_id") val parentCommentId: Long? = null,
) : Serializable {

  fun toDomain(): Comment =
      Comment(
          id = CommentId(id),
          content = content,
          publishDate = publishDate,
          authorId = UserId(authorId),
          modId = ModId(modId),
          parentCommentId = parentCommentId?.let(::CommentId),
      )

  companion object {
    fun fromComment(comment: Comment): CommentEntity =
        CommentEntity(
            comment.id.value,
            comment.content,
            comment.publishDate,
            comment.authorId.value,
          comment.modId.value,
          comment.parentCommentId?.value)
  }
}
