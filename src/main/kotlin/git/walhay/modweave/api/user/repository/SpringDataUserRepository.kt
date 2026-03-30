package git.walhay.modweave.api.user.repository

import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataUserRepository : JpaRepository<UserEntity, UUID> {
  fun existsByLoginIgnoreCase(login: String): Boolean

  fun existsByEmailIgnoreCase(email: String): Boolean

  fun findByLoginIgnoreCase(login: String): UserEntity?

  fun findAllByUsernameContainingIgnoreCase(name: String, pageable: Pageable): Page<UserEntity>
}
