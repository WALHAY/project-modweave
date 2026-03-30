package git.walhay.modweave.api.user.repository

import git.walhay.modweave.api.user.User
import git.walhay.modweave.api.user.toEntity
import java.util.*
import kotlin.jvm.optionals.getOrNull
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class JpaUserRepository(private val repository: SpringDataUserRepository) : UserRepository {
  override fun existsByLogin(login: String): Boolean = repository.existsByLoginIgnoreCase(login)

  override fun existsByEmail(email: String): Boolean = repository.existsByEmailIgnoreCase(email)

  override fun findByLogin(login: String): User? =
      repository.findByLoginIgnoreCase(login)?.toModel()

  override fun findById(id: UUID): User? = repository.findById(id).getOrNull()?.toModel()

  override fun findAll(pageable: Pageable): Page<User> =
      repository.findAll(pageable).map { it.toModel() }

  override fun findAll(name: String, pageable: Pageable): Page<User> =
      repository.findAllByUsernameContainingIgnoreCase(name, pageable).map { it.toModel() }

  override fun save(user: User): User = repository.save<UserEntity>(user.toEntity()).toModel()
}
