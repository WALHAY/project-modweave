package git.walhay.modweave.api.image

import git.walhay.modweave.api.common.HttpCache
import git.walhay.modweave.domain.game.GameId
import git.walhay.modweave.domain.game.GameService
import git.walhay.modweave.domain.mod.ModId
import git.walhay.modweave.domain.mod.ModService
import git.walhay.modweave.domain.storage.ISimpleStorageService
import git.walhay.modweave.domain.storage.ImageFormat
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class ImageController(
    private val mods: ModService,
    private val games: GameService,
    private val storage: ISimpleStorageService,
) {
  @GetMapping("/mods/{modId}/image", version = "2")
  fun getModImage(
      @PathVariable modId: String,
      request: HttpServletRequest,
      response: HttpServletResponse
  ) = writeImage(mods.findModById(ModId(modId)).imagePath, request, response)

  @GetMapping("/games/{gameId}/image", version = "2")
  fun getGameImage(
      @PathVariable gameId: String,
      request: HttpServletRequest,
      response: HttpServletResponse
  ) = writeImage(games.findGameById(GameId(gameId)).imagePath, request, response)

  private fun writeImage(key: String, request: HttpServletRequest, response: HttpServletResponse) {
    // Object keys are immutable, and resource visibility has already been checked.
    val etag = HttpCache.etag("image:$key".toByteArray())
    if (HttpCache.check(request, response, etag)) return
    storage.downloadImage(key).buffered().use { input ->
      // Read only the header and rewind; old objects may have incorrect S3 Content-Type.
      input.mark(8)
      val format = ImageFormat.detect(input)
      input.reset()
      response.contentType = format.contentType
      response.setHeader("Content-Disposition", "inline")
      response.setHeader("X-Content-Type-Options", "nosniff")
      if (request.method != "HEAD") input.copyTo(response.outputStream)
    }
  }
}
