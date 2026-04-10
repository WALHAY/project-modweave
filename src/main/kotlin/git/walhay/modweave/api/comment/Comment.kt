package git.walhay.modweave.api.comment

import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.user.UserId
import java.time.LocalDateTime

data class Comment(
    val id: CommentId = CommentId(),
    val content: String = "",
    val publishDate: LocalDateTime = LocalDateTime.now(),
    val authorId: UserId = UserId(),
    val modId: ModId = ModId()
)
