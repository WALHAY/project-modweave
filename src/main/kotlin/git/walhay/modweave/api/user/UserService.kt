package git.walhay.modweave.api.user

import git.walhay.modweave.api.user.dto.UserRegisterDto
import git.walhay.modweave.api.user.dto.UserUpdateDto
import git.walhay.modweave.api.user.exception.UserEmailExistsException
import git.walhay.modweave.api.user.exception.UserLoginExistsException
import git.walhay.modweave.api.user.exception.UserNotFoundException
import git.walhay.modweave.api.user.repository.UserRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : IUserService {

  override fun findUserById(id: UUID): User =
      userRepository.findById(id) ?: throw UserNotFoundException("User with id=$id not found")

  override fun findUserByLogin(login: String): User =
      userRepository.findByLogin(login)
          ?: throw UserNotFoundException("User with login=$login not found")

  override fun findUsersWithFilter(page: Int, size: Int, name: String?, sort: Sort): Page<User> {
    val pageRequest = PageRequest.of(page, size, sort)
    if (name == null) {
      return userRepository.findAll(pageRequest)
    }
    return userRepository.findAll(name, pageRequest)
  }

  override fun registerNewUser(register: UserRegisterDto): User {
    if (userRepository.existsByLogin(register.login)) {
      throw UserLoginExistsException("Login ${register.login} already in use")
    }

    if (userRepository.existsByEmail(register.email)) {
      throw UserEmailExistsException("Email ${register.email} already in use")
    }

    val encodedPass =
        passwordEncoder.encode(register.password)
            ?: throw IllegalStateException("Failed to encode password")

    val user = User(register.login, register.username, register.email, encodedPass)

    return userRepository.save(user)
  }

  override fun updateUserProfile(id: UUID, update: UserUpdateDto): User {
    val user: User = findUserById(id)

    update.username?.let { user.username = it }
    update.password?.let {
      user.password =
          passwordEncoder.encode(it) ?: throw IllegalStateException("Failed to encode password")
    }
    update.email?.let {
      if (it != user.email && userRepository.existsByEmail(it)) {
        throw UserEmailExistsException("Email $it already in use")
      }
      user.email = it
    }

    return userRepository.save(user)
  }
}
