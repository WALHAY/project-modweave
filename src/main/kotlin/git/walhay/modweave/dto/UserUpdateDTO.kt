package git.walhay.modweave.dto

import jakarta.annotation.Nullable
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size

data class UserUpdateDTO(
    @Nullable @Size(min = 3) val username: String? = null,
    @Nullable @Size(min = 3) val password: String? = null,
    @Nullable @Email val email: String? = null,
) {
  init {
    if (username == null && password == null && email == null) {
      throw Exception("One of the parameters must be set")
    }
  }
}
