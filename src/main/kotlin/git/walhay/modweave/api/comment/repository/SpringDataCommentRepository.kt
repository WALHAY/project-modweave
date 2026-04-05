package git.walhay.modweave.api.comment.repository

import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataCommentRepository : JpaRepository<CommentEntity, Long> {}
