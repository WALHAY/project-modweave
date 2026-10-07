package git.walhay.modweave.api.comment.http

import git.walhay.modweave.api.comment.CommentId
import git.walhay.modweave.api.comment.ICommentService
import git.walhay.modweave.api.comment.exception.CommentNotFoundException
import git.walhay.modweave.api.comment.http.dto.CommentCreateDto
import git.walhay.modweave.api.comment.http.dto.CommentResponseDto
import git.walhay.modweave.api.common.paging.Page
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.user.UserId
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import java.net.URI
import java.util.UUID
import mu.KLogger
import mu.KotlinLogging
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/comments")
class CommentController(
    private val service: ICommentService,
) {
  private val logger: KLogger = KotlinLogging.logger {}

  @GetMapping(version = "2")
  fun getComments(
      @RequestParam modId: String,
      @RequestParam @Min(0) page: Int,
      @RequestParam @Min(1) size: Int,
  ): Page<CommentResponseDto> =
      service.findCommentsByMod(ModId(modId), page, size).map { CommentResponseDto.fromComment(it) }

  @GetMapping("/{id}", version = "2")
  fun getComment(@PathVariable id: UUID): CommentResponseDto =
      service.findCommentById(CommentId(id))?.let { CommentResponseDto.fromComment(it) }
          ?: throw CommentNotFoundException(CommentId(id))

  @PostMapping(
      version = "1", consumes = ["application/x-www-form-urlencoded", "multipart/form-data"])
  fun createComment(
      @ModelAttribute @Valid dto: CommentCreateDto,
      @AuthenticationPrincipal user: UserDetails?,
  ): CommentResponseDto {
    val authenticatedUser = requireUser(user)
    logger.info {
      "POST /comments - creating comment for mod: ${dto.modId} by user: ${authenticatedUser.username}"
    }
    return service
        .createComment(UserId(authenticatedUser.username), dto.toCommentCreateCommand())
        .let { CommentResponseDto.fromComment(it) }
  }

  @PostMapping(version = "2", consumes = ["application/x-www-form-urlencoded"])
  fun createCommentV2(
      @ModelAttribute @Valid dto: CommentCreateDto,
      @AuthenticationPrincipal user: UserDetails?,
  ): ResponseEntity<CommentResponseDto> {
    val result = createComment(dto, user)
    return ResponseEntity.created(URI.create("/api/v2/comments/${result.id}")).body(result)
  }

  @DeleteMapping("/{id}", version = "1")
  fun deleteComment(
      @PathVariable id: UUID,
      @AuthenticationPrincipal user: UserDetails?,
  ) {
    val authenticatedUser = requireUser(user)
    logger.info { "DELETE /comments/$id for user: ${authenticatedUser.username}" }
    service.deleteComment(UserId(authenticatedUser.username), CommentId(id))
  }

  @DeleteMapping("/{id}", version = "2")
  @ResponseStatus(NO_CONTENT)
  fun removeComment(@PathVariable id: UUID, @AuthenticationPrincipal user: UserDetails?) =
      deleteComment(id, user)

  private fun requireUser(user: UserDetails?): UserDetails =
      user ?: throw ResponseStatusException(UNAUTHORIZED, "Authentication required")
}
