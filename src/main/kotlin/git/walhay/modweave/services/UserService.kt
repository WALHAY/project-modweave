package git.walhay.modweave.services

import git.walhay.modweave.dto.UserRegisterDTO
import git.walhay.modweave.dto.UserUpdateDTO
import git.walhay.modweave.models.User
import git.walhay.modweave.repositories.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
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

    val user =
        User(
            register.login.lowercase(),
            register.username,
            register.email,
            passwordEncoder.encode(register.password)!!)
    userRepository.save(user)

    return user
  }

  fun updateUserProfile(login: String, updateDTO: UserUpdateDTO): User {
    val user: User = userRepository.findByIdOrNull(login) ?: throw Exception()

    updateDTO.username?.let { user.username = it }
    updateDTO.password?.let { user.password = passwordEncoder.encode(it)!! }
    updateDTO.email?.let {
      if (userRepository.existsByEmailIgnoreCase(it)) {
        throw Exception("Email already used")
      }
      user.email = it
    }

    userRepository.save(user)

    return user
  }
}
