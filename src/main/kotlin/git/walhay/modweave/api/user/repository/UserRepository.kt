package git.walhay.modweave.api.user.repository

import git.walhay.modweave.api.user.User
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface UserRepository {
  fun existsByLogin(login: String): Boolean

  fun existsByEmail(email: String): Boolean

  fun findByLogin(login: String): User?

  fun findById(id: UUID): User?

  fun findAll(pageable: Pageable): Page<User>

  fun findAll(name: String, pageable: Pageable): Page<User>

  fun save(user: User): User
}
