package git.walhay.modweave.domain.user

class UserLoginExistsException(
    userId: UserId,
) : RuntimeException("User with login=\"$userId\" already exists")
