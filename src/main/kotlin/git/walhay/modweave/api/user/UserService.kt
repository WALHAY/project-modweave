package git.walhay.modweave.api.user

import git.walhay.modweave.api.user.dto.UserDto
import git.walhay.modweave.api.user.dto.UserRegisterDto
import git.walhay.modweave.api.user.dto.UserUpdateDto
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

  fun findUserById(login: String): UserDto =
      userRepository.findByLoginIgnoreCase(login)?.toUserDto()
          ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User with login=$login not found")

  fun registerNewUser(register: UserRegisterDto): UserDto {
    if (userRepository.existsByLoginIgnoreCase(register.login)) {
      throw ResponseStatusException(HttpStatus.CONFLICT, "Login ${register.login} already in use")
    }

    if (userRepository.existsByEmailIgnoreCase(register.email)) {
      throw ResponseStatusException(HttpStatus.CONFLICT, "Email ${register.email} already in use")
    }

    val encodedPass =
        passwordEncoder.encode(register.password)
            ?: throw IllegalStateException("Failed to encode password")

    val user = User(register.login, register.username, register.email, encodedPass)

    return userRepository.save(user).toUserDto()
  }

  fun updateUserProfile(login: String, update: UserUpdateDto): UserDto {
    val user: User = userRepository.findByLoginIgnoreCase(login) ?: throw Exception()

    update.username?.let { user.username = it }
    update.password?.let {
      user.password =
          passwordEncoder.encode(it) ?: throw IllegalStateException("Failed to encode password")
    }
    update.email?.let {
      if (it != user.email && userRepository.existsByEmailIgnoreCase(it)) {
        throw ResponseStatusException(HttpStatus.CONFLICT, "Email $it already in use")
      }
      user.email = it
    }

    return userRepository.save(user).toUserDto()
  }
}
