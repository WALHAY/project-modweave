package git.walhay.modweave.api.user

import git.walhay.modweave.api.common.ApiResource
import git.walhay.modweave.domain.user.User
import java.time.LocalDateTime

data class UserResponseDto(
    val username: String,
    val name: String,
    val registerDate: LocalDateTime,
) : ApiResource {
  companion object {
    fun fromUser(user: User): UserResponseDto =
        UserResponseDto(
            username = user.username.value,
            name = user.name,
            registerDate = user.registerDate,
        )
  }
}
