package git.walhay.modweave.api.auth.http.dto

data class TokenResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
)
