package git.walhay.modweave.api.user.repository

import git.walhay.modweave.api.user.User
import git.walhay.modweave.api.user.UserId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
@org.springframework.context.annotation.Profile("postgres")
class JpaUserRepository(
    private val repository: SpringDataUserRepository,
) : UserRepository {
  override fun existsByUsername(userId: UserId): Boolean =
      repository.existsByUsernameIgnoreCase(userId.value)

  override fun existsByEmail(email: String): Boolean = repository.existsByEmailIgnoreCase(email)

  override fun findByUsername(userId: UserId): User? =
      repository.findByUsernameIgnoreCase(userId.value)?.toDomain()

  override fun findAll(pageable: Pageable): Page<User> =
      repository.findAll(pageable).map { it.toDomain() }

  override fun findAll(
      name: String,
      pageable: Pageable,
  ): Page<User> = repository.findAllByNameContainingIgnoreCase(name, pageable).map { it.toDomain() }

  override fun save(user: User): User =
      repository.save<UserEntity>(UserEntity.fromUser(user)).toDomain()
}
