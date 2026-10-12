package git.walhay.modweave.api.security

import com.nimbusds.jose.jwk.source.ImmutableSecret
import com.nimbusds.jose.proc.SecurityContext
import java.time.Duration
import java.time.Instant
import java.util.Base64
import javax.crypto.spec.SecretKeySpec
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.*

@Configuration
class JwtConfiguration(private val properties: JwtProperties) {
  private val key =
      Base64.getDecoder().decode(properties.secret).let {
        require(it.size >= 32) { "JWT secret must contain at least 32 decoded bytes" }
        SecretKeySpec(it, "HmacSHA256")
      }

  @Bean fun jwtEncoder(): JwtEncoder = NimbusJwtEncoder(ImmutableSecret<SecurityContext>(key))

  @Bean @Primary fun jwtDecoder(): JwtDecoder = decoder("access")

  @Bean fun refreshJwtDecoder(): JwtDecoder = decoder("refresh")

  private fun decoder(type: String): JwtDecoder =
      NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build().apply {
        setJwtValidator(
            DelegatingOAuth2TokenValidator(
                JwtTimestampValidator(Duration.ZERO),
                JwtIssuerValidator(properties.issuer),
                JwtClaimValidator<String>("type") { it == type },
                JwtClaimValidator<String>(JwtClaimNames.SUB) { !it.isNullOrBlank() },
                JwtClaimValidator<Instant>(JwtClaimNames.EXP) { it != null },
                JwtClaimValidator<Instant>(JwtClaimNames.IAT) {
                  it != null && !it.isAfter(Instant.now())
                },
            ))
      }
}
