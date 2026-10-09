package git.walhay.modweave.regression

import git.walhay.modweave.api.comment.ICommentService
import git.walhay.modweave.api.comment.http.CommentController
import git.walhay.modweave.api.common.http.GlobalExceptionHandler
import git.walhay.modweave.api.game.IGameService
import git.walhay.modweave.api.game.http.GameController
import git.walhay.modweave.api.user.http.dto.UserCreateDto
import git.walhay.modweave.api.user.http.dto.UserUpdateDto
import git.walhay.modweave.util.ImageExtensionValidator
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
    val gameService = mock(IGameService::class.java)
    val commentService = mock(ICommentService::class.java)
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
    verify(gameService).deleteGame(git.walhay.modweave.api.game.GameId("example"))
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
