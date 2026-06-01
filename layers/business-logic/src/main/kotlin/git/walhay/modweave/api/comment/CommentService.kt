package git.walhay.modweave.api.comment

import git.walhay.modweave.api.comment.command.CommentCreateCommand
import git.walhay.modweave.api.comment.exception.CommentNotFoundException
import git.walhay.modweave.api.comment.repository.CommentRepository
import git.walhay.modweave.api.user.IUserService
import git.walhay.modweave.api.user.UserId
import jakarta.transaction.Transactional
import mu.KLogger
import mu.KotlinLogging
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
@Transactional
class CommentService(
    private val commentRepository: CommentRepository,
    private val userService: IUserService,
) : ICommentService {
  private val logger: KLogger = KotlinLogging.logger {}

  @Cacheable("comments", key = "#id.value")
  override fun findCommentById(id: CommentId): Comment? {
    logger.debug { "Fetching comment by id: $id" }
    return commentRepository.findById(id) ?: throw CommentNotFoundException(id)
  }

  @CachePut("comments", key = "#result.id.value")
  @PreAuthorize("isAuthenticated()")
  override fun createComment(
      userId: UserId,
      command: CommentCreateCommand,
  ): Comment {
    logger.info { "Creating new comment for mod: ${command.modId} by user: $userId" }
    val user = userService.findUserByUsername(userId)

    return command
        .let { (content, modId) ->
          Comment(content = content, modId = modId, authorId = user.username)
        }
        .let { commentRepository.save(it) }
        .also { logger.info { "Comment created successfully: ${it.id}" } }
  }

  @CacheEvict("comments", key = "#id.value")
  @PreAuthorize("@accessSecurity.isCommentOwnerOrAdmin(#userId, #id)")
  override fun deleteComment(
      userId: UserId,
      id: CommentId,
  ) {
    logger.info { "Deleting comment: $id for user: $userId" }
    commentRepository.delete(id)
    logger.info { "Comment deleted successfully: $id" }
  }
}
