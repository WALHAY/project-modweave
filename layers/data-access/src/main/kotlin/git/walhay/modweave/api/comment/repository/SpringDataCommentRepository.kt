package git.walhay.modweave.api.comment.repository

import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataCommentRepository : JpaRepository<CommentEntity, UUID>
