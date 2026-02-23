package git.walhay.modweave.controller

import git.walhay.modweave.exception.GameNotFoundException
import git.walhay.modweave.exception.ModNotFoundException
import git.walhay.modweave.model.Mod
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ModControllerAdvice {

  @ExceptionHandler(ModNotFoundException::class, GameNotFoundException::class)
  fun notFoundHandler(): ResponseEntity<Mod> {
    return ResponseEntity.notFound().build()
  }
}
