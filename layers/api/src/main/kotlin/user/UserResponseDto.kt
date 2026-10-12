package git.walhay.modweave.api.user

import git.walhay.modweave.api.common.ApiLink
import git.walhay.modweave.api.common.ApiLinkContext
import git.walhay.modweave.api.common.ApiResource
import git.walhay.modweave.domain.user.User
import java.time.LocalDateTime

data class UserResponseDto(
    val username: String,
    val name: String,
    val registerDate: LocalDateTime,
    override val links: Map<String, ApiLink> = emptyMap(),
) : ApiResource {
  override fun withLinks(context: ApiLinkContext): UserResponseDto =
      with(context) {
        copy(
            links =
                buildMap {
                  put("self", link("/users/{id}", username))
                  put("mods", collection("/users/{id}/mods", username))
                  put("collections", collection("/users/{id}/collections", username))
                  if (isSelf(username)) put("update", link("/users/me", method = "PATCH"))
                })
      }

  companion object {
    fun fromUser(user: User): UserResponseDto =
        UserResponseDto(
            username = user.username.value,
            name = user.name,
            registerDate = user.registerDate,
        )
  }
}
