package git.walhay.modweave.api.category

import git.walhay.modweave.api.category.exception.CategoryExistsException
import git.walhay.modweave.api.category.exception.CategoryNotFoundException
import mu.KLogger
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class CategoryExceptionHandler(
    private val logger: KLogger = KotlinLogging.logger {},
) {
  @ExceptionHandler(CategoryNotFoundException::class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  fun categoryNotFoundHandler(e: CategoryNotFoundException) {
    logger.warn { "Category not found: ${e.message}" }
    logger.debug { e }
  }

  @ExceptionHandler(CategoryExistsException::class)
  @ResponseStatus(HttpStatus.CONFLICT)
  fun categoryExistsHandler(e: CategoryExistsException) {
    logger.warn { "Category creation failed: category already exists - ${e.message}" }
    logger.debug { e }
  }
}
