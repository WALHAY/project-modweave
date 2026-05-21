package git.walhay.modweave.api.comment.http

import git.walhay.modweave.api.comment.CommentId
import git.walhay.modweave.api.comment.ICommentService
import git.walhay.modweave.api.comment.http.dto.CommentCreateDto
import git.walhay.modweave.api.comment.http.dto.CommentResponseDto
import git.walhay.modweave.api.user.UserId
import jakarta.validation.Valid
import mu.KLogger
import mu.KotlinLogging
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController("/comments")
class CommentController(
    private val service: ICommentService,
) {
  private val logger: KLogger = KotlinLogging.logger {}

  @PostMapping
  fun createComment(
      @ModelAttribute @Valid dto: CommentCreateDto,
      @AuthenticationPrincipal user: UserDetails,
  ) {
    logger.info {
      "POST /comments - creating comment for mod: ${dto.modId} by user: ${user.username}"
    }
    return service.createComment(UserId(user.username), dto.toCommentCreateCommand()).let {
      CommentResponseDto.fromComment(it)
    }
  }

  @DeleteMapping("/{id}")
  fun deleteComment(
      @PathVariable id: Long,
      @AuthenticationPrincipal user: UserDetails,
  ) {
    logger.info { "DELETE /comments/$id for user: ${user.username}" }
    service.deleteComment(UserId(user.username), CommentId(id))
  }
}
