package git.walhay.modweave.api.user.http.dto

import git.walhay.modweave.api.user.command.UserUpdateCommand
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UserUpdateDto(
    @field:Size(min = 1, max = 100) @field:Pattern(regexp = ".*\\S.*") val name: String? = null,
    @field:Size(min = 8, max = 72) val password: String? = null,
    @field:Email @field:Size(min = 1, max = 320) val email: String? = null,
) {
  @get:AssertTrue(message = "At least one field must be supplied")
  val isUpdatePresent: Boolean
    get() = name != null || password != null || email != null

  @get:AssertTrue(message = "Password must fit within 72 UTF-8 bytes")
  val isPasswordSizeValid: Boolean
    get() = password == null || password.toByteArray(Charsets.UTF_8).size <= 72

  fun toUserUpdateCommand(): UserUpdateCommand =
      UserUpdateCommand(name?.trim(), password, email?.trim()?.lowercase())
}
