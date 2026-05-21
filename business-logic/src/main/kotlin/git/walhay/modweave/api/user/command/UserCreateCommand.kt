package git.walhay.modweave.api.user.command

import git.walhay.modweave.api.user.UserId

data class UserCreateCommand(
    val username: UserId,
    val name: String,
    val password: String,
    val email: String,
)
