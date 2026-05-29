package git.walhay.modweave.api.comment.http.dto

import git.walhay.modweave.api.comment.Comment
import java.time.LocalDateTime

data class CommentResponseDto(
    val id: Long,
    val content: String,
    val publishDate: LocalDateTime,
    val authorId: String,
    val modId: String,
    val parentCommentId: Long?,
) {
  companion object {
    fun fromComment(comment: Comment): CommentResponseDto =
        CommentResponseDto(
            id = comment.id.value,
            content = comment.content,
            publishDate = comment.publishDate,
            authorId = comment.authorId.value,
            modId = comment.modId.value,
            parentCommentId = comment.parentCommentId?.value,
        )
  }
}
