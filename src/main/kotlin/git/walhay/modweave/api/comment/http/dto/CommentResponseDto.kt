package git.walhay.modweave.api.comment.http.dto

import git.walhay.modweave.api.comment.Comment
import io.mcarle.konvert.api.KonvertFrom
import java.time.LocalDateTime

@KonvertFrom(Comment::class)
data class CommentResponseDto(
    val content: String,
    val publishDate: LocalDateTime,
    val authorId: String,
) {
  companion object
}
