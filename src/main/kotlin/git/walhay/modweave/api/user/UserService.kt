package git.walhay.modweave.api.user

import git.walhay.modweave.api.user.command.UserCreateCommand
import git.walhay.modweave.api.user.command.UserUpdateCommand
import git.walhay.modweave.api.user.exception.UserEmailExistsException
import git.walhay.modweave.api.user.exception.UserLoginExistsException
import git.walhay.modweave.api.user.exception.UserNotFoundException
import git.walhay.modweave.api.user.repository.UserRepository
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

  override fun findUserByUsername(userId: UserId): User =
      userRepository.findByUsername(userId) ?: throw UserNotFoundException(userId)

  override fun findUsersWithFilter(page: Int, size: Int, name: String?, sort: Sort): Page<User> {
    val pageRequest = PageRequest.of(page, size, sort)
    if (name == null) {
      return userRepository.findAll(pageRequest)
    }
    return userRepository.findAll(name, pageRequest)
  }

  override fun registerNewUser(command: UserCreateCommand): User {
    if (userRepository.existsByUsername(command.username)) {
      throw UserLoginExistsException(command.username)
    }

    if (userRepository.existsByEmail(command.email)) {
      throw UserEmailExistsException(command.email)
    }

    val encodedPass =
        passwordEncoder.encode(command.password)
            ?: throw IllegalStateException("Failed to encode password")

    return command
        .let { (username, name, _, email) -> User(username, name, email, encodedPass) }
        .let { userRepository.save(it) }
  }

  override fun updateUserProfile(userId: UserId, command: UserUpdateCommand): User {
    val user: User = findUserByUsername(userId)

    command.username?.let { user.name = it }
    command.password?.let {
      user.password =
          passwordEncoder.encode(it) ?: throw IllegalStateException("Failed to encode password")
    }
    command.email?.let {
      if (it != user.email && userRepository.existsByEmail(it)) {
        throw UserEmailExistsException("Email $it already in use")
      }
      user.email = it
    }

    return userRepository.save(user)
  }
}
