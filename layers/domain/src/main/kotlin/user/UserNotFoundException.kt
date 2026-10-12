package git.walhay.modweave.domain.user

class UserNotFoundException(
    userId: UserId,
) : RuntimeException("User with id=\"$userId\" not found")
