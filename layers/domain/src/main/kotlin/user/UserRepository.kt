package git.walhay.modweave.domain.user

import git.walhay.modweave.domain.common.paging.Page
import git.walhay.modweave.domain.common.paging.PageRequest

interface UserRepository {
  fun existsByUsername(userId: UserId): Boolean

  fun existsByEmail(email: String): Boolean

  fun findByUsername(userId: UserId): User?

  fun findAll(pageRequest: PageRequest): Page<User>

  fun findAll(
      name: String,
      pageRequest: PageRequest,
  ): Page<User>

  fun save(user: User): User
}
