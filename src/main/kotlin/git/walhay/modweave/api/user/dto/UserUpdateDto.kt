package git.walhay.modweave.api.user.dto

import jakarta.annotation.Nullable
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size

data class UserUpdateDto(
    @field:Nullable @field:Size(min = 3) val username: String? = null,
    @field:Nullable @field:Size(min = 3) val password: String? = null,
    @field:Nullable @field:Email val email: String? = null,
) {
  init {
    if (username == null && password == null && email == null) {
      throw Exception("One of the parameters must be set")
    }
  }
}
