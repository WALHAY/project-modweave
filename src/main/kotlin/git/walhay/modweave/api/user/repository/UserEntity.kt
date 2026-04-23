package git.walhay.modweave.api.user.repository

import git.walhay.modweave.api.mod.repository.ModEntity
import git.walhay.modweave.api.user.User
import io.mcarle.konvert.api.KonvertFrom
import io.mcarle.konvert.api.KonvertTo
import jakarta.persistence.*
import java.io.Serializable
import java.time.LocalDateTime
import org.hibernate.annotations.NaturalId

@Entity
@Table(schema = "modweave", name = "users")
@KonvertTo(User::class, mapFunctionName = "toDomain")
@KonvertFrom(User::class)
class UserEntity(
    @Id @Column(name = "username", nullable = false, length = 50) var username: String,
    @Column(name = "name", unique = true, nullable = false, length = 100) var name: String,
    @NaturalId(mutable = true)
    @Column(name = "email", unique = true, nullable = false, length = 320)
    var email: String,
    @Column(name = "password", nullable = false, length = 255) var password: String,
    @Column(name = "register_date", nullable = false) val registerDate: LocalDateTime,
    @Column(name = "is_admin", nullable = false) val isAdmin: Boolean = false,
    @OneToMany(mappedBy = "publisherId", fetch = FetchType.LAZY, orphanRemoval = true)
    val mods: MutableSet<ModEntity> = mutableSetOf(),
) : Serializable {
  constructor() : this("", "", "", "")

  constructor(
      username: String,
      name: String,
      email: String,
      password: String,
  ) : this(username, name, email, password, LocalDateTime.now(), false)

  @PrePersist
  @PreUpdate
  fun normalize() {
    username = username.lowercase().trim()
    email = email.lowercase().trim()
  }

  companion object
}
