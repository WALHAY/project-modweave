package git.walhay.modweave.api.auth

import jakarta.validation.constraints.NotBlank

data class RefreshTokenRequestDto(
    @field:NotBlank val refreshToken: String,
)
