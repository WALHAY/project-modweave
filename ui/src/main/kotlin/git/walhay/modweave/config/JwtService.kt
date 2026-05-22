package git.walhay.modweave.config

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import java.time.Instant
import java.util.Date
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service

@Service
class JwtService(
    private val properties: JwtProperties,
) {
  private val key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret))

  fun generateAccessToken(user: UserDetails): String =
      generateToken(user, "access", properties.accessTokenTtl)

  fun generateRefreshToken(user: UserDetails): String =
      generateToken(user, "refresh", properties.refreshTokenTtl)

  fun extractUsername(token: String): String = parseClaims(token).subject

  fun extractRoles(token: String): List<String> =
      (parseClaims(token)["roles"] as? Collection<*>)?.filterIsInstance<String>() ?: emptyList()

  fun isAccessTokenValid(
      token: String,
      user: UserDetails,
  ): Boolean = isTokenValid(token, user, "access")

  fun isRefreshTokenValid(
      token: String,
      user: UserDetails,
  ): Boolean = isTokenValid(token, user, "refresh")

  private fun generateToken(
      user: UserDetails,
      type: String,
      ttl: java.time.Duration,
  ): String {
    val now = Instant.now()
    val roles = user.authorities.map { it.authority }
    return Jwts.builder()
        .subject(user.username)
        .issuer(properties.issuer)
        .claim("roles", roles)
        .claim("type", type)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(ttl)))
        .signWith(key)
        .compact()
  }

  private fun isTokenValid(
      token: String,
      user: UserDetails,
      expectedType: String,
  ): Boolean {
    val claims = parseClaims(token)
    val type = claims["type"] as? String ?: return false
    return claims.subject == user.username && type == expectedType
  }

  private fun parseClaims(token: String): Claims =
      Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
}
