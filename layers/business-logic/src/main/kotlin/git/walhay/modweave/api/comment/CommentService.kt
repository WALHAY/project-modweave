package git.walhay.modweave.api.comment

import git.walhay.modweave.api.comment.command.CommentCreateCommand
import git.walhay.modweave.api.comment.exception.CommentNotFoundException
import git.walhay.modweave.api.comment.repository.CommentRepository
import git.walhay.modweave.api.common.paging.Page
import git.walhay.modweave.api.common.paging.PageSizePolicy
import git.walhay.modweave.api.mod.IModService
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.mod.exception.ModNotFoundException
import git.walhay.modweave.api.user.IUserService
import git.walhay.modweave.api.user.UserId
import jakarta.transaction.Transactional
import mu.KLogger
import mu.KotlinLogging
import org.springframework.cache.annotation.CacheEvict
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
@Transactional
class CommentService(
    private val commentRepository: CommentRepository,
    private val userService: IUserService,
    private val pageSizePolicy: PageSizePolicy,
    private val modService: IModService,
) : ICommentService {
  private val logger: KLogger = KotlinLogging.logger {}

  override fun findCommentsByMod(modId: ModId, page: Int, size: Int): Page<Comment> {
    try {
      modService.findModById(modId)
    } catch (_: ModNotFoundException) {
      return Page(emptyList(), page, pageSizePolicy.normalize(size), 0, 0)
    }
    return commentRepository.findByModId(
        modId, PageRequest.of(page, pageSizePolicy.normalize(size), Sort.by("publishDate", "id")))
  }

  override fun findCommentById(id: CommentId): Comment? {
    logger.debug { "Fetching comment by id: $id" }
    val comment = commentRepository.findById(id) ?: throw CommentNotFoundException(id)
    modService.findModById(comment.modId)
    return comment
  }

  @CacheEvict("comments", allEntries = true)
  @PreAuthorize("@accessSecurity.isSelf(#p0)")
  override fun createComment(
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
  override fun deleteComment(
      userId: UserId,
      id: CommentId,
  ) {
    logger.info { "Deleting comment: $id for user: $userId" }
    commentRepository.delete(id)
    logger.info { "Comment deleted successfully: $id" }
  }
}
