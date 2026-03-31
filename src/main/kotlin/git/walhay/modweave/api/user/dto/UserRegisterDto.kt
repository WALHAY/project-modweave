package git.walhay.modweave.api.user.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserRegisterDto(
    @field:NotBlank @field:Size(min = 3) val username: String,
    @field:NotBlank val name: String,
    @field:NotBlank @field:Size(min = 8) val password: String,
    @field:Email val email: String
)
