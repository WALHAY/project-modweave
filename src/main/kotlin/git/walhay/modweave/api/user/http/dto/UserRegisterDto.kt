package git.walhay.modweave.api.user.http.dto

import git.walhay.modweave.api.user.command.UserCreateCommand
import io.mcarle.konvert.api.KonvertTo
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@KonvertTo(UserCreateCommand::class)
data class UserRegisterDto(
    @field:NotBlank @field:Size(min = 3) val username: String,
    @field:NotBlank val name: String,
    @field:NotBlank @field:Size(min = 8) val password: String,
    @field:Email val email: String
)
