package git.walhay.modweave.api.user.repository

import git.walhay.modweave.api.user.User
import git.walhay.modweave.api.user.toEntity
import org.springframework.stereotype.Repository

@Repository
class JpaUserRepository(private val repository: SpringDataUserRepository) : UserRepository {
	override fun existsByLogin(login: String): Boolean = repository.existsByLoginIgnoreCase(login)

	override fun existsByEmail(email: String): Boolean = repository.existsByEmailIgnoreCase(email)

	override fun findByLogin(login: String): User? = repository.findByLoginIgnoreCase(login)?.toDto()

    override fun save(user: User): User = repository.save<UserEntity>(user.toEntity()).toDto()
}
