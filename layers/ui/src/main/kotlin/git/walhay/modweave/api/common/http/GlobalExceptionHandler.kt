package git.walhay.modweave.api.common.http

import mu.KLogger
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler(
    private val logger: KLogger = KotlinLogging.logger {},
) {
  @ExceptionHandler(AuthorizationDeniedException::class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  fun authorizationException(e: Exception) {
    logger.info { "Authorization failed" } 
  }
}
