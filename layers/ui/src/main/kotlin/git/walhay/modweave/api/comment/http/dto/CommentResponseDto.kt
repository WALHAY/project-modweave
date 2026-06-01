package git.walhay.modweave.api.comment.http.dto

import git.walhay.modweave.api.comment.Comment
import java.time.LocalDateTime

data class CommentResponseDto(
    val content: String,
    val publishDate: LocalDateTime,
    val authorId: String,
) {
  companion object {
    fun fromComment(comment: Comment): CommentResponseDto =
        CommentResponseDto(
            content = comment.content,
            publishDate = comment.publishDate,
            authorId = comment.authorId.value,
        )
  }
}
