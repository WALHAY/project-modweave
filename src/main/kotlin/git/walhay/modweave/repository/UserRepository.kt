package git.walhay.modweave.repository

import git.walhay.modweave.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, String> {
  fun existsByLoginIgnoreCase(login: String): Boolean

  fun existsByEmailIgnoreCase(email: String): Boolean

  fun findByLoginIgnoreCase(login: String): User?
}
