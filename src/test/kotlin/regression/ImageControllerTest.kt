package git.walhay.modweave.regression

import git.walhay.modweave.api.image.ImageController
import git.walhay.modweave.domain.game.GameId
import git.walhay.modweave.domain.game.GameService
import git.walhay.modweave.domain.mod.*
import git.walhay.modweave.domain.storage.ISimpleStorageService
import git.walhay.modweave.domain.user.UserId
import java.io.ByteArrayInputStream
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class ImageControllerTest {
  @Test
  fun `image checks visibility before conditional response and streams contents with detected MIME`() {
    val mods = mock(ModService::class.java)
    val storage = mock(ISimpleStorageService::class.java)
    val controller = ImageController(mods, mock(GameService::class.java), storage)
    val mod =
        Mod(
            ModId("example"),
            "Example",
            imagePath = "key",
            publisherId = UserId("owner"),
            gameId = GameId("game"))
    `when`(mods.findModById(mod.id)).thenReturn(mod)
    val bytes =
        java.io
            .ByteArrayOutputStream()
            .also {
              javax.imageio.ImageIO.write(
                  java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_RGB),
                  "png",
                  it)
            }
            .toByteArray()
    var closed = 0
    `when`(storage.downloadImage("key")).thenAnswer {
      object : ByteArrayInputStream(bytes) {
        override fun close() {
          closed++
          super.close()
        }
      }
    }
    val response = MockHttpServletResponse()
    controller.getModImage(
        "example", MockHttpServletRequest("GET", "/api/v2/mods/example/image"), response)
    assertArrayEquals(bytes, response.contentAsByteArray)
    assertEquals("image/png", response.contentType)
    assertEquals(1, closed)
    clearInvocations(storage)
    val request =
        MockHttpServletRequest("GET", "/api/v2/mods/example/image").apply {
          addHeader("If-None-Match", requireNotNull(response.getHeader("ETag")))
        }
    val cached = MockHttpServletResponse()
    controller.getModImage("example", request, cached)
    assertEquals(304, cached.status)
    assertEquals(0, cached.contentAsByteArray.size)
    verifyNoInteractions(storage)
    `when`(mods.findModById(mod.id)).thenThrow(ModNotFoundException(mod.id))
    assertThrows(ModNotFoundException::class.java) {
      controller.getModImage("example", request, MockHttpServletResponse())
    }
    verifyNoInteractions(storage)
  }
}
