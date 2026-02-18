package git.walhay.modweave.dto

import jakarta.annotation.Nullable
import jakarta.validation.constraints.Email

data class UserUpdateDTO(
    @Nullable val username: String? = null,
    @Nullable val password: String? = null,
    @Email @Nullable val email: String? = null,
) {
  init {
    if (username == null && password == null && email == null) {
      throw Exception("One of the parameters must be set")
    }
  }
}
