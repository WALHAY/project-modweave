package git.walhay.modweave.api.comment.repository

import git.walhay.modweave.api.comment.Comment
import io.mcarle.konvert.api.KonvertFrom
import io.mcarle.konvert.api.KonvertTo
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(schema = "modweave", name = "comments")
@KonvertTo(Comment::class, mapFunctionName = "toDomain")
@KonvertFrom(Comment::class)
class CommentEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column("id") val id: Long = 0,
    @Column("content", nullable = false) val content: String = "",
    @Column("publish_date", nullable = false) val publishDate: LocalDateTime = LocalDateTime.now(),
    @Column("user_id", nullable = false) val authorId: String = "",
    @Column("mod_id", nullable = false) val modId: String = ""
) {
    companion object
}
