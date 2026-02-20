package git.walhay.modweave.model

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(schema = "modweave", name = "users")
data class User(
    @Id
    @Column(name = "login", nullable = false, length = 50)
    var login: String,

    @Column(name = "username", unique = true, nullable = false, length = 100)
    var username: String,

    @Column(name = "email", unique = true, nullable = false, length = 320)
    var email: String,

    @Column(name = "password", nullable = false, length = 255)
    @JsonBackReference
    var password: String,

    @Column(name = "register_date", nullable = false)
    val registerDate: LocalDateTime,

    @Column(name = "is_admin", nullable = false)
    val isAdmin: Boolean = false,

    @OneToMany(mappedBy = "publisher", fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference("user-mods")
    val mods: MutableSet<Mod> = mutableSetOf()
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
