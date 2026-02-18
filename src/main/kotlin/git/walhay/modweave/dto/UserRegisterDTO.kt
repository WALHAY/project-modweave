package git.walhay.modweave.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserRegisterDTO(
    @NotBlank @Size(min = 3) val login: String,
    @NotBlank val username: String,
    @NotBlank @Size(min = 8) val password: String,
    @Email val email: String
)
