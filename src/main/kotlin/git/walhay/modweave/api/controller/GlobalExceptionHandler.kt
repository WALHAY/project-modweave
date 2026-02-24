package git.walhay.modweave.api.controller

import git.walhay.modweave.api.game.exception.GameNotFoundException
import git.walhay.modweave.api.mod.exception.ModNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

  @ExceptionHandler(ModNotFoundException::class, GameNotFoundException::class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  fun notFoundHandler() {}
}
