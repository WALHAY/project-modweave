package git.walhay.modweave.api.user.repository

import git.walhay.modweave.api.user.User
import git.walhay.modweave.api.user.UserId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface UserRepository {
  fun existsByUsername(username: UserId): Boolean

  fun existsByEmail(email: String): Boolean

  fun findByUsername(username: UserId): User?

  fun findAll(pageable: Pageable): Page<User>

  fun findAll(name: String, pageable: Pageable): Page<User>

  fun save(user: User): User
}
