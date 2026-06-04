package git.walhay.modweave.api.comment.http

import git.walhay.modweave.api.comment.CommentId
import git.walhay.modweave.api.comment.ICommentService
import git.walhay.modweave.api.comment.http.dto.CommentCreateDto
import git.walhay.modweave.api.comment.http.dto.CommentResponseDto
import git.walhay.modweave.api.user.UserId
import jakarta.validation.Valid
import java.util.UUID
import mu.KLogger
import mu.KotlinLogging
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController("/comments")
class CommentController(
    private val service: ICommentService,
) {
  private val logger: KLogger = KotlinLogging.logger {}

  @PostMapping
  fun createComment(
      @ModelAttribute @Valid dto: CommentCreateDto,
      @AuthenticationPrincipal user: UserDetails?,
  ) {
    val authenticatedUser = requireUser(user)
    logger.info {
      "POST /comments - creating comment for mod: ${dto.modId} by user: ${authenticatedUser.username}"
    }
    return service
        .createComment(UserId(authenticatedUser.username), dto.toCommentCreateCommand())
        .let { CommentResponseDto.fromComment(it) }
  }

  @DeleteMapping("/{id}")
  fun deleteComment(
      @PathVariable id: UUID,
      @AuthenticationPrincipal user: UserDetails?,
  ) {
    val authenticatedUser = requireUser(user)
    logger.info { "DELETE /comments/$id for user: ${authenticatedUser.username}" }
    service.deleteComment(UserId(authenticatedUser.username), CommentId(id))
  }

  private fun requireUser(user: UserDetails?): UserDetails =
      user ?: throw ResponseStatusException(UNAUTHORIZED, "Authentication required")
}
