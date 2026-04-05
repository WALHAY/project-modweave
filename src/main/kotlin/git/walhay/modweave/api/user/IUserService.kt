package git.walhay.modweave.api.user

import git.walhay.modweave.api.user.command.UserCreateCommand
import git.walhay.modweave.api.user.command.UserUpdateCommand
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort

interface IUserService {
  fun findUserByUsername(userId: UserId): User

  fun findUsersWithFilter(page: Int, size: Int, name: String?, sort: Sort): Page<User>

  fun createUser(command: UserCreateCommand): User

  fun updateUser(userId: UserId, command: UserUpdateCommand): User
}
