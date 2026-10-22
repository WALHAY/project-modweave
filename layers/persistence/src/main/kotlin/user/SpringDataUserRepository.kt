package git.walhay.modweave.persistence.user

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataUserRepository : JpaRepository<UserEntity, String> {
  fun existsByUsernameIgnoreCase(username: String): Boolean

  fun existsByEmailIgnoreCase(email: String): Boolean

  fun findByUsernameIgnoreCase(username: String): UserEntity?

  fun findAllByUsernameContainingIgnoreCase(
      username: String,
      pageable: Pageable,
  ): Page<UserEntity>
}
