package git.walhay.modweave.api.user.dto

import git.walhay.modweave.api.user.UserId
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserRegisterDto(
    @field:NotBlank @field:Size(min = 3) val username: UserId,
    @field:NotBlank val name: String,
    @field:NotBlank @field:Size(min = 8) val password: String,
    @field:Email val email: String
)
