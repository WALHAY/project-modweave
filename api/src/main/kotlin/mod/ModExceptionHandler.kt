package git.walhay.modweave.api.mod

import git.walhay.modweave.domain.mod.ModExistsException
import git.walhay.modweave.domain.mod.ModNotFoundException
import mu.KLogger
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ModExceptionHandler(
    private val logger: KLogger = KotlinLogging.logger {},
) {
  @ExceptionHandler(ModNotFoundException::class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  fun modNotFoundHandler(e: ModNotFoundException) {
    logger.warn { "Mod not found: ${e.message}" }
    logger.debug { e }
  }

  @ExceptionHandler(ModExistsException::class)
  @ResponseStatus(HttpStatus.CONFLICT)
  fun modExistsHandler(e: ModExistsException) {
    logger.warn { "Mod creation failed: mod already exists - ${e.message}" }
    logger.debug { e }
  }
}
