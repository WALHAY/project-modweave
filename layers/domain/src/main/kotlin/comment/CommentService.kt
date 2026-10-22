package git.walhay.modweave.domain.comment

import git.walhay.modweave.domain.common.paging.Page
import git.walhay.modweave.domain.common.paging.PageRequest
import git.walhay.modweave.domain.common.paging.PageSizePolicy
import git.walhay.modweave.domain.common.paging.SortOrder
import git.walhay.modweave.domain.mod.ModId
import git.walhay.modweave.domain.mod.ModNotFoundException
import git.walhay.modweave.domain.mod.ModService
import git.walhay.modweave.domain.user.UserId
import git.walhay.modweave.domain.user.UserService
import mu.KLogger
import mu.KotlinLogging
import org.springframework.cache.annotation.CacheEvict
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val userService: UserService,
    private val pageSizePolicy: PageSizePolicy,
    private val modService: ModService,
) {
  private val logger: KLogger = KotlinLogging.logger {}

  fun findCommentsByMod(modId: ModId, page: Int, size: Int): Page<Comment> {
    try {
      modService.findModById(modId)
    } catch (_: ModNotFoundException) {
      return Page(emptyList(), page, pageSizePolicy.normalize(size), 0, 0)
    }
    return commentRepository.findByModId(
        modId,
        PageRequest(
            page,
            pageSizePolicy.normalize(size),
            listOf(SortOrder("publishDate"), SortOrder("id"))))
  }

  fun findCommentById(id: CommentId): Comment {
    logger.debug { "Fetching comment by id: $id" }
    val comment = commentRepository.findById(id) ?: throw CommentNotFoundException(id)
    modService.findModById(comment.modId)
    return comment
  }

  @CacheEvict("comments", allEntries = true)
  @PreAuthorize("@accessSecurity.isSelf(#p0)")
  @Transactional(rollbackFor = [Exception::class])
  fun createComment(
      userId: UserId,
      command: CommentCreateCommand,
  ): Comment {
    logger.info { "Creating new comment for mod: ${command.modId} by user: $userId" }
    modService.findModById(command.modId)
    val user = userService.findUserByUsername(userId)

    return command
        .let { (content, modId) ->
          Comment(content = content, modId = modId, authorId = user.username)
        }
        .let { commentRepository.save(it) }
        .also { logger.info { "Comment created successfully: ${it.id}" } }
  }

  @CacheEvict("comments", key = "#p1")
  @PreAuthorize("@accessSecurity.isCommentOwnerOrAdmin(#p0, #p1)")
  @Transactional(rollbackFor = [Exception::class])
  fun deleteComment(
      userId: UserId,
      id: CommentId,
  ) {
    logger.info { "Deleting comment: $id for user: $userId" }
    commentRepository.delete(id)
    logger.info { "Comment deleted successfully: $id" }
  }
}
