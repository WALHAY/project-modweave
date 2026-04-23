package git.walhay.modweave.api.game

import git.walhay.modweave.api.game.exception.GameExistsException
import git.walhay.modweave.api.game.exception.GameNotFoundException
import mu.KLogger
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GameExceptionHandler(
    private val logger: KLogger = KotlinLogging.logger {},
) {
  @ExceptionHandler(GameExistsException::class)
  @ResponseStatus(HttpStatus.CONFLICT)
  fun gameExistsHandler(e: GameExistsException) {
    logger.error { e }
  }

  @ExceptionHandler(GameNotFoundException::class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  fun gameNotFoundHandler(e: GameNotFoundException) {
    logger.error { e }
  }
}
