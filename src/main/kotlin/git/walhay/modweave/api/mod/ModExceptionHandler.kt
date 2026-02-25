package git.walhay.modweave.api.mod

import git.walhay.modweave.api.mod.exception.ModNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ModExceptionHandler {

  @ExceptionHandler(ModNotFoundException::class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  fun modNotFoundHandler() {}
}
