package git.walhay.modweave.regression

import git.walhay.modweave.api.comment.CommentController
import git.walhay.modweave.api.common.GlobalExceptionHandler
import git.walhay.modweave.api.game.GameController
import git.walhay.modweave.api.user.UserCreateDto
import git.walhay.modweave.api.user.UserUpdateDto
import git.walhay.modweave.api.util.ImageExtensionValidator
import git.walhay.modweave.domain.comment.CommentService
import git.walhay.modweave.domain.game.GameService
import jakarta.validation.Validation
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.accept.ApiVersionResolver
import org.springframework.web.accept.DefaultApiVersionStrategy
import org.springframework.web.accept.SemanticApiVersionParser

class HttpAndValidationTest {
  @Test
  fun `game deletion binds path and comment creation has its own route`() {
    val gameService = mock(GameService::class.java)
    val commentService = mock(CommentService::class.java)
    val mvc =
        MockMvcBuilders.standaloneSetup(
                GameController(gameService), CommentController(commentService))
            .setApiVersionStrategy(
                DefaultApiVersionStrategy(
                    listOf(ApiVersionResolver { "1" }),
                    SemanticApiVersionParser(),
                    false,
                    "1",
                    true,
                    null,
                    null))
            .setCustomArgumentResolvers(
                org.springframework.security.web.method.annotation
                    .AuthenticationPrincipalArgumentResolver())
            .setControllerAdvice(GlobalExceptionHandler())
            .build()
    mvc.perform(delete("/games/example")).andExpect(status().isOk)
    verify(gameService).deleteGame(git.walhay.modweave.domain.game.GameId("example"))
    mvc.perform(
            post("/comments")
                .contentType("application/x-www-form-urlencoded")
                .param("content", "Hello")
                .param("modId", "example"))
        .andExpect(status().isUnauthorized)
  }

  @Test
  fun `missing invalid and empty images are handled without exceptions`() {
    val validator = ImageExtensionValidator()
    assertFalse(validator.isValid(null, null))
    assertFalse(validator.isValid(MockMultipartFile("image", "", null, byteArrayOf()), null))
    assertFalse(
        validator.isValid(
            MockMultipartFile("image", "fake.png", "image/png", "not an image".toByteArray()),
            null))
    val output = java.io.ByteArrayOutputStream()
    javax.imageio.ImageIO.write(
        java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_RGB),
        "png",
        output)
    assertTrue(
        validator.isValid(
            MockMultipartFile("image", "real.PNG", "image/png", output.toByteArray()), null))
    val corruptPng =
        java.util.Base64.getDecoder()
            .decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAusB9Wl6sEAAAAAASUVORK5CYII=")
    assertFalse(
        validator.isValid(MockMultipartFile("image", "corrupt.png", "image/png", corruptPng), null))
    assertFalse(
        validator.isValid(
            MockMultipartFile("image", "wrong.gif", "image/gif", output.toByteArray()), null))
  }

  @Test
  fun `image validation bounds dimensions before decoding and matches extensions to content`() {
    val output = java.io.ByteArrayOutputStream()
    javax.imageio.ImageIO.write(
        java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_RGB),
        "png",
        output)
    val png = output.toByteArray()
    val validator = ImageExtensionValidator()
    assertTrue(validator.isValid(MockMultipartFile("image", "real.PNG", "text/plain", png), null))
    assertFalse(validator.isValid(MockMultipartFile("image", "fake.jpg", "image/jpeg", png), null))
    fun header(width: Int, height: Int): ByteArray {
      val bytes = png.copyOf()
      java.nio.ByteBuffer.wrap(bytes).putInt(16, width).putInt(20, height)
      val crc = java.util.zip.CRC32().apply { update(bytes, 12, 17) }
      java.nio.ByteBuffer.wrap(bytes).putInt(29, crc.value.toInt())
      return bytes
    }
    // A few bytes with a huge declared image size must be rejected without allocating pixels.
    for ((width, height) in listOf(Int.MAX_VALUE to 1, 4097 to 1, 4001 to 4000, 0 to 1)) {
      assertFalse(
          validator.isValid(
              MockMultipartFile("image", "large.png", "image/png", header(width, height)), null))
    }
    header(4000, 4000).inputStream().use {
      assertEquals(
          git.walhay.modweave.domain.storage.ImageFormat.PNG,
          git.walhay.modweave.domain.storage.ImageFormat.inspect(it))
    }
    val jpeg =
        java.io
            .ByteArrayOutputStream()
            .also {
              javax.imageio.ImageIO.write(
                  java.awt.image.BufferedImage(2, 2, java.awt.image.BufferedImage.TYPE_INT_RGB),
                  "jpeg",
                  it)
            }
            .toByteArray()
    assertTrue(
        validator.isValid(
            MockMultipartFile("image", "photo.JPEG", "application/octet-stream", jpeg), null))
  }

  @Test
  fun `profile updates and registration enforce consistent password and email rules`() {
    Validation.buildDefaultValidatorFactory().use { factory ->
      val validator = factory.validator
      assertTrue(validator.validate(UserUpdateDto()).isNotEmpty())
      assertTrue(validator.validate(UserUpdateDto(password = "abc")).isNotEmpty())
      assertTrue(validator.validate(UserUpdateDto(password = "я".repeat(40))).isNotEmpty())
      assertTrue(validator.validate(UserUpdateDto(name = "   ")).isNotEmpty())
      assertTrue(validator.validate(UserUpdateDto(name = "New name")).isEmpty())
      assertTrue(validator.validate(UserCreateDto("name", "Name", "password", "")).isNotEmpty())
    }
  }
}
