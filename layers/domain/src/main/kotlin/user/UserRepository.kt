package git.walhay.modweave.domain.user

import git.walhay.modweave.domain.common.paging.Page
import org.springframework.data.domain.Pageable

interface UserRepository {
  fun existsByUsername(userId: UserId): Boolean

  fun existsByEmail(email: String): Boolean

  fun findByUsername(userId: UserId): User?

  fun findAll(pageable: Pageable): Page<User>

  fun findAll(
      name: String,
      pageable: Pageable,
  ): Page<User>

  fun save(user: User): User
}
