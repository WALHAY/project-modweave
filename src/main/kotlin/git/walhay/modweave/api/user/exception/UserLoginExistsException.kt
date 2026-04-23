package git.walhay.modweave.api.user.exception

import git.walhay.modweave.api.user.UserId

class UserLoginExistsException(
    userId: UserId,
) : Exception("User with login=\"$userId\" already exists")
