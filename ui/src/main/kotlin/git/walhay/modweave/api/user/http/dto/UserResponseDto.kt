package git.walhay.modweave.api.user.http.dto

import git.walhay.modweave.api.user.User
import java.time.LocalDateTime

data class UserResponseDto(
    val name: String,
    val registerDate: LocalDateTime,
) {
  companion object {
    fun fromUser(user: User): UserResponseDto =
        UserResponseDto(
            name = user.name,
            registerDate = user.registerDate,
        )
  }
}
