package git.walhay.modweave.api.user

import git.walhay.modweave.api.user.dto.UserRegisterDto
import git.walhay.modweave.api.user.dto.UserUpdateDto

interface IUserService {
  fun findUserById(login: String): User

  fun registerNewUser(register: UserRegisterDto): User

  fun updateUserProfile(login: String, update: UserUpdateDto): User
}
