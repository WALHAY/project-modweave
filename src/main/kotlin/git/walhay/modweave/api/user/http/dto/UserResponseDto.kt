package git.walhay.modweave.api.user.http.dto

import git.walhay.modweave.api.user.User
import io.mcarle.konvert.api.KonvertFrom
import java.time.LocalDateTime

@KonvertFrom(User::class)
data class UserResponseDto(val name: String, val registerDate: LocalDateTime) {
  companion object
}
