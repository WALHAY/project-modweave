package git.walhay.modweave.api.comment.http.dto

import java.time.LocalDateTime

data class CommentResponseDto(
    val content: String,
    val publishDate: LocalDateTime,
    val authorId: String
)
