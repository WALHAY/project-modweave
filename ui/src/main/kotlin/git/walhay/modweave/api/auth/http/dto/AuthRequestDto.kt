package git.walhay.modweave.api.auth.http.dto

import jakarta.validation.constraints.NotBlank

data class AuthRequestDto(
    @field:NotBlank val username: String,
    @field:NotBlank val password: String,
)
