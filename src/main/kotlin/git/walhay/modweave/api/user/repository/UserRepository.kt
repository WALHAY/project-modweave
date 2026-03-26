package git.walhay.modweave.api.user.repository

import git.walhay.modweave.api.user.User

interface UserRepository {
  fun existsByLogin(login: String): Boolean

  fun existsByEmail(email: String): Boolean

  fun findByLogin(login: String): User?

  fun save(user: User): User
}
