package git.walhay.modweave.persistence.user

import git.walhay.modweave.domain.common.paging.Page
import git.walhay.modweave.domain.common.paging.PageRequest
import git.walhay.modweave.domain.user.User
import git.walhay.modweave.domain.user.UserId
import git.walhay.modweave.domain.user.UserRepository
import git.walhay.modweave.persistence.common.paging.toDomainPage
import git.walhay.modweave.persistence.common.paging.toSpringPageable
import org.springframework.stereotype.Repository

@Repository
class JpaUserRepository(
    private val repository: SpringDataUserRepository,
) : UserRepository {
  override fun existsByUsername(userId: UserId): Boolean =
      repository.existsByUsernameIgnoreCase(userId.value)

  override fun existsByEmail(email: String): Boolean = repository.existsByEmailIgnoreCase(email)

  override fun findByUsername(userId: UserId): User? =
      repository.findByUsernameIgnoreCase(userId.value)?.toDomain()

  override fun findAll(pageRequest: PageRequest): Page<User> =
      repository.findAll(pageRequest.toSpringPageable()).toDomainPage { it.toDomain() }

  override fun findAll(
      name: String,
      pageRequest: PageRequest,
  ): Page<User> =
      repository
          .findAllByUsernameContainingIgnoreCase(name, pageRequest.toSpringPageable())
          .toDomainPage { it.toDomain() }

  override fun save(user: User): User =
      repository
          .save<UserEntity>(
              UserEntity.fromUser(
                  user.copy(
                      username = UserId(user.username.value.trim().lowercase()),
                      email = user.email.trim().lowercase())))
          .toDomain()
}
