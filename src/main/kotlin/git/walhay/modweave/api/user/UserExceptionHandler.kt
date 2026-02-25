package git.walhay.modweave.api.user

import git.walhay.modweave.api.user.exception.UserEmailExistsException
import git.walhay.modweave.api.user.exception.UserLoginExistsException
import git.walhay.modweave.api.user.exception.UserNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class UserExceptionHandler {

  @ExceptionHandler(UserEmailExistsException::class)
  @ResponseStatus(HttpStatus.CONFLICT)
  fun userEmailExistsHandler(e: UserEmailExistsException) {}

  @ExceptionHandler(UserLoginExistsException::class)
  @ResponseStatus(HttpStatus.CONFLICT)
  fun userLoginExistsHandler(e: UserLoginExistsException) {}

  @ExceptionHandler(UserLoginExistsException::class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  fun userNotFoundException(e: UserNotFoundException) {}
}
