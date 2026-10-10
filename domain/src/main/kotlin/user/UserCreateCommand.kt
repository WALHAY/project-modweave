package git.walhay.modweave.domain.user

data class UserCreateCommand(
    val username: UserId,
    val name: String,
    val password: String,
    val email: String,
)
