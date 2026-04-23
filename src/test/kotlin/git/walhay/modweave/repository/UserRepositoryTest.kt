package git.walhay.modweave.repository

import git.walhay.modweave.api.user.User
import git.walhay.modweave.api.user.repository.JpaUserRepository
import git.walhay.modweave.api.user.repository.UserRepository
import git.walhay.modweave.testutils.PostgresTestTemplate
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@Import(JpaUserRepository::class, BCryptPasswordEncoder::class)
class UserRepositoryTest : PostgresTestTemplate() {
  @Autowired lateinit var userRepository: UserRepository

  @Autowired lateinit var passwordEncoder: BCryptPasswordEncoder

  @Test
  fun `test user insert`() {
    val user = User("walhay", "walhay", "walhay@mail.ru", passwordEncoder.encode("password")!!)

    userRepository.save(user)
  }
}
