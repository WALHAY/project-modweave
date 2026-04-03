package git.walhay.modweave.api.user

import git.walhay.modweave.api.user.dto.UserRegisterDto
import git.walhay.modweave.api.user.dto.UserUpdateDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort

interface IUserService {
  fun findUserByUsername(username: UserId): User

  fun findUsersWithFilter(page: Int, size: Int, name: String?, sort: Sort): Page<User>

  fun registerNewUser(register: UserRegisterDto): User

  fun updateUserProfile(username: UserId, update: UserUpdateDto): User
}
