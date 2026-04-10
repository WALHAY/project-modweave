package git.walhay.modweave.api.comment

import git.walhay.modweave.api.comment.command.CommentCreateCommand
import git.walhay.modweave.api.comment.exception.CommentNotFoundException
import git.walhay.modweave.api.comment.repository.CommentRepository
import git.walhay.modweave.api.user.IUserService
import git.walhay.modweave.api.user.UserId
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
@Transactional
class CommentService(
    private val commentRepository: CommentRepository,
    private val userService: IUserService
) : ICommentService {
  override fun findCommentById(id: CommentId): Comment? =
      commentRepository.findById(id) ?: throw CommentNotFoundException(id)

  override fun createComment(userId: UserId, command: CommentCreateCommand): Comment {
    val user = userService.findUserByUsername(userId)

    return command
        .let { (content, modId) ->
          Comment(content = content, modId = modId, authorId = user.username)
        }
        .let { commentRepository.save(it) }
  }

  override fun deleteComment(userId: UserId, id: CommentId) {
    val comment = findCommentById(id)
    if (comment?.authorId != userId) {
      throw Exception("Forbidden")
    }

    commentRepository.delete(id)
  }
}
