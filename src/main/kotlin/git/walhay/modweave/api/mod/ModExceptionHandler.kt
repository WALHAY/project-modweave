package git.walhay.modweave.api.mod

import git.walhay.modweave.api.mod.exception.ModCreationFailedException
import git.walhay.modweave.api.mod.exception.ModExistsException
import git.walhay.modweave.api.mod.exception.ModNotFoundException
import mu.KLogger
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ModExceptionHandler(private val logger: KLogger = KotlinLogging.logger {}) {

  @ExceptionHandler(ModNotFoundException::class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  fun modNotFoundHandler(e: ModNotFoundException) {
    logger.error { e }
  }

  @ExceptionHandler(ModExistsException::class)
  @ResponseStatus(HttpStatus.CONFLICT)
  fun modExistsHandler(e: ModExistsException) {
    logger.error { e }
  }

  @ExceptionHandler(ModCreationFailedException::class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  fun modCreationFailedHandler(e: ModCreationFailedException) {
    logger.error { e }
  }
}
