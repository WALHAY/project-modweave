package git.walhay.modweave.api.comment

import git.walhay.modweave.api.comment.http.dto.CommentResponseDto
import git.walhay.modweave.api.comment.repository.CommentEntity
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.user.UserId
import io.mcarle.konvert.api.KonvertTo
import java.time.LocalDateTime

@KonvertTo(CommentResponseDto::class, mapFunctionName = "toResponseDto")
@KonvertTo(CommentEntity::class, mapFunctionName = "toEntity")
data class Comment(
    val id: CommentId = CommentId(),
    val content: String = "",
    val publishDate: LocalDateTime = LocalDateTime.now(),
    val authorId: UserId = UserId(),
    val modId: ModId = ModId()
)
