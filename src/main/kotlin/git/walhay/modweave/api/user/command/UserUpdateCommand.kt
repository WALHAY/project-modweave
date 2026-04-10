package git.walhay.modweave.api.user.command

data class UserUpdateCommand(
    val username: String? = null,
    val password: String? = null,
    val email: String? = null
)
