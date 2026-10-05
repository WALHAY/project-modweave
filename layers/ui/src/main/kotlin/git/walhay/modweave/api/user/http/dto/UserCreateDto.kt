package git.walhay.modweave.api.user.http.dto

import git.walhay.modweave.api.user.UserId
import git.walhay.modweave.api.user.command.UserCreateCommand
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UserCreateDto(
    @field:NotBlank
    @field:Size(min = 3, max = 50)
    @field:Pattern(regexp = "[A-Za-z0-9_-]+")
    val username: String,
    @field:NotBlank @field:Size(max = 100) val name: String,
    @field:NotBlank @field:Size(min = 8, max = 72) val password: String,
    @field:NotBlank @field:Email @field:Size(max = 320) val email: String,
) {
  @get:AssertTrue(message = "Password must fit within 72 UTF-8 bytes")
  val isPasswordSizeValid: Boolean
    get() = password.toByteArray(Charsets.UTF_8).size <= 72

  fun toUserCreateCommand(): UserCreateCommand =
      UserCreateCommand(
          UserId(username.trim().lowercase()), name.trim(), password, email.trim().lowercase())
}
