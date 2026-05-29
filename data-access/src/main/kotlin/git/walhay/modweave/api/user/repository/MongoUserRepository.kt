package git.walhay.modweave.api.user.repository

import git.walhay.modweave.api.user.User
import git.walhay.modweave.api.user.UserId
import java.time.LocalDateTime
import org.springframework.context.annotation.Profile
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Document(collection = "users")
data class UserDocument(
    @Id val username: String,
    val name: String,
    val email: String,
    val password: String,
    val registerDate: LocalDateTime = LocalDateTime.now(),
    val isAdmin: Boolean = false,
)

interface SpringDataMongoUserRepository : MongoRepository<UserDocument, String> {
  fun findAllByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<UserDocument>

  fun existsByEmail(email: String): Boolean
}

@Repository
@Profile("mongodb")
class MongoUserRepository(private val repository: SpringDataMongoUserRepository) :
    git.walhay.modweave.api.user.repository.UserRepository {
  private fun UserDocument.toDomain(): User =
      User(
          UserId(this.username),
          this.name,
          this.email,
          this.password,
          this.registerDate,
          this.isAdmin,
          mutableSetOf(),
      )

  private fun User.toDocument(): UserDocument =
      UserDocument(
          this.username.value,
          this.name,
          this.email,
          this.password,
          this.registerDate,
          this.isAdmin)

  override fun existsByUsername(userId: UserId): Boolean = repository.existsById(userId.value)

  override fun existsByEmail(email: String): Boolean = repository.existsByEmail(email)

  override fun findByUsername(userId: UserId): User? =
      repository.findById(userId.value).map { it.toDomain() }.orElse(null)

  override fun findAll(pageable: Pageable): Page<User> =
      repository.findAll(pageable).map { it.toDomain() }

  override fun findAll(name: String, pageable: Pageable): Page<User> =
      repository.findAllByNameContainingIgnoreCase(name, pageable).map { it.toDomain() }

  override fun save(user: User): User = repository.save(user.toDocument()).toDomain()
}
