package git.walhay.modweave.api.category

import git.walhay.modweave.api.category.exception.CategoryExistsException
import git.walhay.modweave.api.category.exception.CategoryNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class CategoryExceptionHandler {

	@ExceptionHandler(CategoryNotFoundException::class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	fun categoryNotFoundHandler(e: CategoryNotFoundException) {
	}

	@ExceptionHandler(CategoryExistsException::class)
	@ResponseStatus(HttpStatus.CONFLICT)
	fun categoryExistsHandler(e: CategoryExistsException) {
	}
}
