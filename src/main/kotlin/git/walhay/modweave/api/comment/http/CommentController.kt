package git.walhay.modweave.api.comment.http

import git.walhay.modweave.api.comment.CommentId
import git.walhay.modweave.api.comment.ICommentService
import git.walhay.modweave.api.comment.http.dto.CommentCreateDto
import git.walhay.modweave.api.comment.http.dto.toCommentCreateCommand
import git.walhay.modweave.api.comment.toResponseDto
import git.walhay.modweave.api.user.UserId
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController("/comments")
class CommentController(private val service: ICommentService) {

  @PostMapping
  fun createComment(
      @ModelAttribute @Valid dto: CommentCreateDto,
      @AuthenticationPrincipal user: UserDetails
  ) = service.createComment(UserId(user.username), dto.toCommentCreateCommand()).toResponseDto()

  @DeleteMapping("/{id}")
  fun deleteComment(@PathVariable id: Long, @AuthenticationPrincipal user: UserDetails) =
      service.deleteComment(UserId(user.username), CommentId(id))
}
