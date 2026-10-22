package git.walhay.modweave.api.auth

import jakarta.validation.constraints.NotBlank

data class AuthRequestDto(
    @field:NotBlank val username: String,
    @field:NotBlank val password: String,
)

data class RefreshTokenRequestDto(
    @field:NotBlank val refreshToken: String,
)

data class TokenResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
)
