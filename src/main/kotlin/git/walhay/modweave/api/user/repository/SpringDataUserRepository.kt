package git.walhay.modweave.api.user.repository

import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataUserRepository : JpaRepository<UserEntity, String> {
  fun existsByLoginIgnoreCase(login: String): Boolean

  fun existsByEmailIgnoreCase(email: String): Boolean

  fun findByLoginIgnoreCase(login: String): UserEntity?
}
