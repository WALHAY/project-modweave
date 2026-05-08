package git.walhay.modweave.api.user

import git.walhay.modweave.api.user.exception.UserEmailExistsException
import git.walhay.modweave.api.user.exception.UserLoginExistsException
import git.walhay.modweave.api.user.exception.UserNotFoundException
import mu.KLogger
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class UserExceptionHandler(
    private val logger: KLogger = KotlinLogging.logger {},
) {
  @ExceptionHandler(UserEmailExistsException::class)
  @ResponseStatus(HttpStatus.CONFLICT)
  fun userEmailExistsHandler(e: UserEmailExistsException) {
    logger.warn { "User registration failed: email already exists - ${e.message}" }
    logger.debug { e }
  }

  @ExceptionHandler(UserLoginExistsException::class)
  @ResponseStatus(HttpStatus.CONFLICT)
  fun userLoginExistsHandler(e: UserLoginExistsException) {
    logger.warn { "User registration failed: login already exists - ${e.message}" }
    logger.debug { e }
  }

  @ExceptionHandler(UserNotFoundException::class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  fun userNotFoundHandler(e: UserNotFoundException) {
    logger.warn { "User not found: ${e.message}" }
    logger.debug { e }
  }
}
