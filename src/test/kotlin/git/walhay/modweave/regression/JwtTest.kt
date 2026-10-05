package git.walhay.modweave.regression

import git.walhay.modweave.api.auth.http.AuthController
import git.walhay.modweave.api.auth.http.dto.RefreshTokenRequestDto
import git.walhay.modweave.config.*
import java.time.Duration
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.mock.web.*
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.*
import org.springframework.web.server.ResponseStatusException

class JwtTest {
  private val properties =
      JwtProperties(
          "ZmFrZS1zZWNyZXQtc2VjdXJlLWtleS1mb3ItanVzdC1kZXZlbG9wbWVudA==",
          "modweave",
          Duration.ofMinutes(15),
          Duration.ofDays(30))
  private val jwt = JwtService(properties)
  private val user = User.withUsername("owner").password("password").roles("USER").build()

  @AfterEach
  fun cleanup() {
    SecurityContextHolder.clearContext()
  }

  @Test
  fun `token types and issuers are enforced`() {
    assertTrue(jwt.isAccessTokenValid(jwt.generateAccessToken(user), user))
    assertFalse(jwt.isAccessTokenValid(jwt.generateRefreshToken(user), user))
    assertFalse(jwt.isRefreshTokenValid(jwt.generateAccessToken(user), user))
    val foreign = JwtService(properties.copy(issuer = "other"))
    assertThrows(io.jsonwebtoken.JwtException::class.java) {
      jwt.isAccessTokenValid(foreign.generateAccessToken(user), user)
    }
  }

  @Test
  fun `disabled accounts cannot use tokens`() {
    val disabled = User.withUserDetails(user).disabled(true).build()
    assertFalse(jwt.isAccessTokenValid(jwt.generateAccessToken(user), disabled))
  }

  @Test
  fun `filter uses current roles instead of stale token roles`() {
    val admin = User.withUserDetails(user).roles("ADMIN").build()
    val request =
        MockHttpServletRequest().apply {
          addHeader("Authorization", "Bearer ${jwt.generateAccessToken(admin)}")
        }
    JwtAuthenticationFilter(jwt, UserDetailsService { user })
        .doFilter(request, MockHttpServletResponse(), MockFilterChain())
    assertEquals(
        listOf("ROLE_USER"),
        SecurityContextHolder.getContext().authentication!!.authorities.map { it.authority })
  }

  @Test
  fun `deleted account and malformed token do not cause server error`() {
    for (token in listOf(jwt.generateAccessToken(user), "broken")) {
      val request = MockHttpServletRequest().apply { addHeader("Authorization", "Bearer $token") }
      val chain = MockFilterChain()
      JwtAuthenticationFilter(
              jwt, UserDetailsService { throw UsernameNotFoundException("deleted") })
          .doFilter(request, MockHttpServletResponse(), chain)
      assertNotNull(chain.request)
      assertNull(SecurityContextHolder.getContext().authentication)
    }
  }

  @Test
  fun `bad refresh tokens produce unauthorized responses`() {
    val controller =
        AuthController(mock(AuthenticationManager::class.java), UserDetailsService { user }, jwt)
    for (token in listOf("broken", jwt.generateAccessToken(user))) {
      val error =
          assertThrows(ResponseStatusException::class.java) {
            controller.refresh(RefreshTokenRequestDto(token))
          }
      assertEquals(401, error.statusCode.value())
    }
  }
}
