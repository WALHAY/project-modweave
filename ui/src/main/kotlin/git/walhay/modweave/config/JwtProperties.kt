package git.walhay.modweave.config

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.Duration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "security.jwt")
data class JwtProperties(
    @field:NotBlank val secret: String,
    @field:NotBlank val issuer: String,
    @field:NotNull val accessTokenTtl: Duration,
    @field:NotNull val refreshTokenTtl: Duration,
)
