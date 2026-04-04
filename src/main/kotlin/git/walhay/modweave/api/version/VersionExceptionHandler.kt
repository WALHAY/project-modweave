package git.walhay.modweave.api.version

import git.walhay.modweave.api.version.exception.VersionExistsException
import git.walhay.modweave.api.version.exception.VersionNotFoundException
import mu.KLogger
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class VersionExceptionHandler(private val logger: KLogger = KotlinLogging.logger {}) {

  @ExceptionHandler(VersionNotFoundException::class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  fun versionNotFoundHandler(e: VersionNotFoundException) {
    logger.error { e }
  }

  @ExceptionHandler(VersionExistsException::class)
  @ResponseStatus(HttpStatus.CONFLICT)
  fun versionNotFoundHandler(e: VersionExistsException) {
    logger.error { e }
  }
}
