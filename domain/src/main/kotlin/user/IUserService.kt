package git.walhay.modweave.domain.user

import git.walhay.modweave.domain.common.paging.Page
import org.springframework.data.domain.Sort

interface IUserService {
  fun findUserForAuthentication(userId: UserId): User

  fun findUserByUsername(userId: UserId): User

  fun findUsersWithFilter(
      page: Int,
      size: Int,
      name: String?,
      sort: Sort,
  ): Page<User>

  fun createUser(command: UserCreateCommand): User

  fun updateUser(
      userId: UserId,
      command: UserUpdateCommand,
  ): User
}
