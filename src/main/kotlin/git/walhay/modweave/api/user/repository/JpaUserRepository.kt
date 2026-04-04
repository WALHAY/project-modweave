package git.walhay.modweave.api.user.repository

import git.walhay.modweave.api.user.User
import git.walhay.modweave.api.user.UserId
import git.walhay.modweave.api.user.toEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class JpaUserRepository(private val repository: SpringDataUserRepository) : UserRepository {
  override fun existsByUsername(userId: UserId): Boolean =
      repository.existsByUsernameIgnoreCase(userId.value)

  override fun existsByEmail(email: String): Boolean = repository.existsByEmailIgnoreCase(email)

  override fun findByUsername(userId: UserId): User? =
      repository.findByUsernameIgnoreCase(userId.value)?.toModel()

  override fun findAll(pageable: Pageable): Page<User> =
      repository.findAll(pageable).map { it.toModel() }

  override fun findAll(name: String, pageable: Pageable): Page<User> =
      repository.findAllByNameContainingIgnoreCase(name, pageable).map { it.toModel() }

  override fun save(user: User): User = repository.save<UserEntity>(user.toEntity()).toModel()
}
