package git.walhay.modweave.domain.user

class UserEmailExistsException(
    email: String,
) : RuntimeException("User with email=\"$email\" already exists")
