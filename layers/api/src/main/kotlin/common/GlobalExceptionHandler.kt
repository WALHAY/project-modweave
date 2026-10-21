package git.walhay.modweave.api.common

import git.walhay.modweave.domain.category.CategoryExistsException
import git.walhay.modweave.domain.category.CategoryNotFoundException
import git.walhay.modweave.domain.collection.CollectionNotFoundException
import git.walhay.modweave.domain.comment.CommentNotFoundException
import git.walhay.modweave.domain.file.FileNotFoundException
import git.walhay.modweave.domain.game.GameExistsException
import git.walhay.modweave.domain.game.GameNotFoundException
import git.walhay.modweave.domain.mod.ModExistsException
import git.walhay.modweave.domain.mod.ModNotFoundException
import git.walhay.modweave.domain.storage.ImageNotFoundException
import git.walhay.modweave.domain.user.UserEmailExistsException
import git.walhay.modweave.domain.user.UserLoginExistsException
import git.walhay.modweave.domain.user.UserNotFoundException
import git.walhay.modweave.domain.version.VersionExistsException
import git.walhay.modweave.domain.version.VersionNotFoundException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {
  @ExceptionHandler(AccessDeniedException::class)
  fun accessDenied(): ProblemDetail =
      ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Access denied")

  @ExceptionHandler(AuthenticationException::class)
  fun authenticationFailed(): ProblemDetail =
      ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid credentials")

  @ExceptionHandler(IllegalArgumentException::class)
  fun invalidArgument(e: IllegalArgumentException): ProblemDetail =
      ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message ?: "Invalid request")

  @ExceptionHandler(
      CategoryNotFoundException::class,
      CollectionNotFoundException::class,
      CommentNotFoundException::class,
      FileNotFoundException::class,
      ImageNotFoundException::class,
      GameNotFoundException::class,
      ModNotFoundException::class,
      UserNotFoundException::class,
      VersionNotFoundException::class)
  fun notFound(): ProblemDetail =
      ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Resource not found")

  @ExceptionHandler(
      DataIntegrityViolationException::class,
      CategoryExistsException::class,
      GameExistsException::class,
      ModExistsException::class,
      UserEmailExistsException::class,
      UserLoginExistsException::class,
      VersionExistsException::class)
  fun conflict(): ProblemDetail =
      ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Request conflicts with existing data")
}
