package git.walhay.modweave.api.version

import git.walhay.modweave.api.version.exception.VersionNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class VersionExceptionHandler {

	@ExceptionHandler(VersionNotFoundException::class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	fun versionNotFoundHandler(e: VersionNotFoundException) {
	}
}
