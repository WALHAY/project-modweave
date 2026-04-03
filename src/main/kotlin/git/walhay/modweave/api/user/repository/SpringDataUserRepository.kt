package git.walhay.modweave.api.user.repository

import git.walhay.modweave.api.user.UserId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataUserRepository : JpaRepository<UserEntity, UserId> {
  fun existsByUsernameIgnoreCase(userId: UserId): Boolean

  fun existsByEmailIgnoreCase(email: String): Boolean

  fun findByUsernameIgnoreCase(userId: UserId): UserEntity?

  fun findAllByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<UserEntity>
}
