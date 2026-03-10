package git.walhay.modweave.api.user.repository

import git.walhay.modweave.api.mod.repository.ModEntity
import git.walhay.modweave.api.user.User
import io.mcarle.konvert.api.KonvertTo
import jakarta.persistence.*
import org.hibernate.annotations.NaturalId
import java.time.LocalDateTime

@Entity
@Table(schema = "modweave", name = "users")
@KonvertTo(User::class, mapFunctionName = "toModel")
class UserEntity(
    @Id @Column(name = "login", nullable = false, length = 50) var login: String,
    @Column(name = "username", unique = true, nullable = false, length = 100) var username: String,
    @NaturalId(mutable = true)
	@Column(name = "email", unique = true, nullable = false, length = 320) var email: String,
    @Column(name = "password", nullable = false, length = 255) var password: String,
    @Column(name = "register_date", nullable = false) val registerDate: LocalDateTime,
    @Column(name = "is_admin", nullable = false) val isAdmin: Boolean = false,
    @OneToMany(mappedBy = "publisher", fetch = FetchType.LAZY, orphanRemoval = true)
	val mods: MutableSet<ModEntity> = mutableSetOf()
) {
	constructor() : this("", "", "", "")

	constructor(
		login: String,
		username: String,
		email: String,
		password: String
	) : this(login, username, email, password, LocalDateTime.now(), false)

	@PrePersist
	@PreUpdate
	fun normalize() {
		login = login.lowercase().trim()
		email = email.lowercase().trim()
	}
}