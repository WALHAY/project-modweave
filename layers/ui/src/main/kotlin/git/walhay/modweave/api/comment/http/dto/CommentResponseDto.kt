package git.walhay.modweave.api.comment.http.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import git.walhay.modweave.api.comment.Comment
import java.time.LocalDateTime
import java.util.UUID

data class CommentResponseDto(
    val id: UUID,
    val content: String,
    val publishDate: LocalDateTime,
    val authorId: String,
    @get:JsonIgnore val modId: String = "",
) {
  companion object {
    fun fromComment(comment: Comment): CommentResponseDto =
        CommentResponseDto(
            id = comment.id.value,
            content = comment.content,
            publishDate = comment.publishDate,
            authorId = comment.authorId.value,
            modId = comment.modId.value,
        )
  }
}
