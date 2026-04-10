package git.walhay.modweave.api.user.exception

import git.walhay.modweave.api.user.UserId

class UserNotFoundException(userId: UserId) : Exception("User with id=\"$userId\" not found")
