package git.walhay.modweave.domain.comment

import git.walhay.modweave.domain.mod.ModId
import git.walhay.modweave.domain.user.UserId
import java.io.Serializable
import java.time.LocalDateTime

data class Comment(
    val id: CommentId = CommentId(),
    val content: String = "",
    val publishDate: LocalDateTime = LocalDateTime.now(),
    val authorId: UserId = UserId(),
    val modId: ModId = ModId(),
) : Serializable
