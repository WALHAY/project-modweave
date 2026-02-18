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

  fun registerNewUser(registerDTO: UserRegisterDTO): User {
    if (userRepository.existsByLoginIgnoreCase(registerDTO.login)) {
      throw Exception()
    }

    val user =
        User(
            registerDTO.login.lowercase(),
            registerDTO.username,
            registerDTO.email,
            passwordEncoder.encode(registerDTO.password)!!)
    userRepository.save(user)

    return user
  }

  fun updateUserProfile(login: String, updateDTO: UserUpdateDTO): User {
    val user: User = userRepository.findByIdOrNull(login) ?: throw Exception()

    updateDTO.username?.let { user.username = it }
    updateDTO.password?.let { user.password = passwordEncoder.encode(it)!! }
    updateDTO.email?.let { user.email = it }

    userRepository.save(user)

    return user
  }
}
