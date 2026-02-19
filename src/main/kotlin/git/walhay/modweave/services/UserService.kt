package git.walhay.modweave.services

import git.walhay.modweave.dto.UserRegisterDTO
import git.walhay.modweave.dto.UserUpdateDTO
import git.walhay.modweave.models.User
import git.walhay.modweave.repositories.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserService
@Autowired
constructor(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

  fun registerNewUser(register: UserRegisterDTO): User {
    if (userRepository.existsByLoginIgnoreCase(register.login)) {
      throw Exception("Login already used")
    }

    if (userRepository.existsByEmailIgnoreCase(register.email)) {
      throw Exception("Email already used")
    }

      val encodedPass = passwordEncoder.encode(register.password) ?: throw Exception("Failed to encode password")

    val user =
        User(
            register.login,
            register.username,
            register.email,
            encodedPass
            )

    return userRepository.save(user)
  }

  fun updateUserProfile(login: String, update: UserUpdateDTO): User {
    val user: User = userRepository.findByLoginIgnoreCase(login) ?: throw Exception()


    update.username?.let { user.username = it }
    update.password?.let { user.password = passwordEncoder.encode(it) ?: throw Exception("Unable to encode password") }
    update.email?.let {
      if (userRepository.existsByEmailIgnoreCase(it)) {
        throw Exception("Email already used")
      }
      user.email = it
    }

    return userRepository.save(user)
  }
}
