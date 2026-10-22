package git.walhay.modweave.regression

import git.walhay.modweave.api.auth.AuthController
import git.walhay.modweave.api.auth.RefreshTokenRequestDto
import git.walhay.modweave.api.security.*
import java.time.Duration
import java.time.Instant
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.userdetails.*
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.*
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException
import org.springframework.web.server.ResponseStatusException

class JwtTest {
  private val properties =
      JwtProperties(
          "ZmFrZS1zZWNyZXQtc2VjdXJlLWtleS1mb3ItanVzdC1kZXZlbG9wbWVudA==",
          "modweave",
          Duration.ofMinutes(15),
          Duration.ofDays(30))
  private val config = JwtConfiguration(properties)
  private val jwt = JwtService(properties, config.jwtEncoder(), config.refreshJwtDecoder())
  private val user = User.withUsername("owner").password("password").roles("USER").build()

  @Test
  fun `access and refresh token types and issuers are enforced`() {
    assertEquals("owner", config.jwtDecoder().decode(jwt.generateAccessToken(user)).subject)
    assertEquals("owner", jwt.extractRefreshSubject(jwt.generateRefreshToken(user)))
    assertThrows(JwtException::class.java) {
      config.jwtDecoder().decode(jwt.generateRefreshToken(user))
    }
    assertThrows(JwtException::class.java) {
      jwt.extractRefreshSubject(jwt.generateAccessToken(user))
    }
    val foreign = JwtConfiguration(properties.copy(issuer = "other"))
    assertThrows(JwtException::class.java) {
      foreign.jwtDecoder().decode(jwt.generateAccessToken(user))
    }
  }

  @Test
  fun `invalid signature expiry and missing required claims are rejected`() {
    val foreign =
        JwtConfiguration(
            properties.copy(
                secret = java.util.Base64.getEncoder().encodeToString(ByteArray(32) { 42 })))
    assertThrows(JwtException::class.java) {
      foreign.jwtDecoder().decode(jwt.generateAccessToken(user))
    }
    assertThrows(JwtException::class.java) { config.jwtDecoder().decode("broken") }
    fun token(expiration: Instant?, subject: String? = "owner"): String {
      val claims =
          JwtClaimsSet.builder()
              .issuer(properties.issuer)
              .issuedAt(Instant.now().minusSeconds(60))
              .claim("type", "access")
      expiration?.let { claims.expiresAt(it) }
      subject?.let { claims.subject(it) }
      return config
          .jwtEncoder()
          .encode(
              JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims.build()))
          .tokenValue
    }
    for (bad in
        listOf(
            token(Instant.now().minusSeconds(1)),
            token(null),
            token(Instant.now().plusSeconds(60), null))) {
      assertThrows(JwtException::class.java) { config.jwtDecoder().decode(bad) }
    }
  }

  @Test
  fun `disabled locked and deleted accounts cannot use tokens`() {
    val decoded = config.jwtDecoder().decode(jwt.generateAccessToken(user))
    for (unavailable in
        listOf(
            User.withUserDetails(user).disabled(true).build(),
            User.withUserDetails(user).accountLocked(true).build())) {
      assertThrows(InvalidBearerTokenException::class.java) {
        CurrentUserJwtConverter(UserDetailsService { unavailable }).convert(decoded)
      }
    }
    assertThrows(InvalidBearerTokenException::class.java) {
      CurrentUserJwtConverter(UserDetailsService { throw UsernameNotFoundException("deleted") })
          .convert(decoded)
    }
  }

  @Test
  fun `authentication uses current roles instead of stale token roles`() {
    val admin = User.withUserDetails(user).roles("ADMIN").build()
    val decoded = config.jwtDecoder().decode(jwt.generateAccessToken(admin))
    val authentication = CurrentUserJwtConverter(UserDetailsService { user }).convert(decoded)
    assertEquals(listOf("ROLE_USER"), authentication.authorities.map { it.authority })
    assertSame(user, authentication.principal)
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
