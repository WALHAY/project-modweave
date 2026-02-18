package git.walhay.modweave.services

import git.walhay.modweave.dto.RegisterForm
import git.walhay.modweave.models.User
import git.walhay.modweave.repositories.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService
@Autowired
constructor(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

  @Transactional
  fun registerNewUser(registerForm: RegisterForm) {
    if (userRepository.existsById(registerForm.login)) {
      throw Exception()
    }

    val user =
        User(
            registerForm.login.lowercase(),
            registerForm.username,
            registerForm.email,
            passwordEncoder.encode(registerForm.password)!!
        )
    userRepository.save(user)
  }
}
