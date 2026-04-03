package git.walhay.modweave.api.user.repository

import git.walhay.modweave.api.user.UserId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface SpringDataUserRepository : JpaRepository<UserEntity, UUID> {
  fun existsByUsernameIgnoreCase(userId: UserId): Boolean

  fun existsByEmailIgnoreCase(email: String): Boolean

  fun findByUsernameIgnoreCase(userId: UserId): UserEntity?

  fun findAllByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<UserEntity>
}
