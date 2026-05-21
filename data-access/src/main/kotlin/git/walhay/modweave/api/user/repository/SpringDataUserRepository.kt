package git.walhay.modweave.api.user.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataUserRepository : JpaRepository<UserEntity, String> {
  fun existsByUsernameIgnoreCase(username: String): Boolean

  fun existsByEmailIgnoreCase(email: String): Boolean

  fun findByUsernameIgnoreCase(username: String): UserEntity?

  fun findAllByNameContainingIgnoreCase(
      name: String,
      pageable: Pageable,
  ): Page<UserEntity>
}
