package git.walhay.modweave.api.auth

data class TokenResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
)
