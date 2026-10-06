package git.walhay.modweave.api.comment.repository

import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataCommentRepository : JpaRepository<CommentEntity, UUID> {
  fun findAllByModId(modId: String, pageable: Pageable): Page<CommentEntity>
}
