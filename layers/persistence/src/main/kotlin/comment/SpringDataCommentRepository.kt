package git.walhay.modweave.persistence.comment

import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataCommentRepository : JpaRepository<CommentEntity, UUID> {
  fun findAllByModId(modId: String, pageable: Pageable): Page<CommentEntity>
}
