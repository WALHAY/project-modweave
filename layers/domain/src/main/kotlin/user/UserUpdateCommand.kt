package git.walhay.modweave.domain.user

data class UserUpdateCommand(
    val name: String? = null,
    val password: String? = null,
    val email: String? = null,
)
