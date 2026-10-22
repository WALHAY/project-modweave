package git.walhay.modweave.domain.user

data class UserCreateCommand(
    val username: UserId,
    val name: String,
    val password: String,
    val email: String,
)

data class UserUpdateCommand(
    val name: String? = null,
    val password: String? = null,
    val email: String? = null,
)

class UserEmailExistsException(
    email: String,
) : RuntimeException("User with email=\"$email\" already exists")

class UserLoginExistsException(
    userId: UserId,
) : RuntimeException("User with login=\"$userId\" already exists")

class UserNotFoundException(
    userId: UserId,
) : RuntimeException("User with id=\"$userId\" not found")
