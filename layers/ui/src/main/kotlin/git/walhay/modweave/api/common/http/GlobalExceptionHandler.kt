package git.walhay.modweave.api.common.http

import git.walhay.modweave.api.collection.exception.CollectionNotFoundException
import git.walhay.modweave.api.comment.exception.CommentNotFoundException
import git.walhay.modweave.api.file.FileNotFoundException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
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
      CollectionNotFoundException::class,
      CommentNotFoundException::class,
      FileNotFoundException::class)
  fun notFound(): ProblemDetail =
      ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Resource not found")

  @ExceptionHandler(DataIntegrityViolationException::class)
  fun conflict(): ProblemDetail =
      ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Request conflicts with existing data")
}
