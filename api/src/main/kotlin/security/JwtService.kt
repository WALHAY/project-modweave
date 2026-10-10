package git.walhay.modweave.api.security

import java.time.Duration
import java.time.Instant
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.*
import org.springframework.stereotype.Service

@Service
class JwtService(
    private val properties: JwtProperties,
    private val encoder: JwtEncoder,
    @Qualifier("refreshJwtDecoder") private val refreshDecoder: JwtDecoder,
) {
  fun generateAccessToken(user: UserDetails): String =
      generateToken(user, "access", properties.accessTokenTtl)

  fun generateRefreshToken(user: UserDetails): String =
      generateToken(user, "refresh", properties.refreshTokenTtl)

  fun extractRefreshSubject(token: String): String = refreshDecoder.decode(token).subject

  private fun generateToken(user: UserDetails, type: String, ttl: Duration): String {
    val now = Instant.now()
    val claims =
        JwtClaimsSet.builder()
            .subject(user.username)
            .issuer(properties.issuer)
            .issuedAt(now)
            .expiresAt(now.plus(ttl))
            .claim("roles", user.authorities.map { it.authority })
            .claim("type", type)
            .build()
    return encoder
        .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
        .tokenValue
  }
}
