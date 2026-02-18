package git.walhay.modweave.models

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*
import java.sql.Timestamp

@Entity(name = "users")
@Table(schema = "modweave", name = "users")
data class User(
    @Id @Column(name = "login") val login: String,
    @Column(name = "username", unique = true, nullable = false) var username: String,
    @Column(name = "email", unique = true, nullable = false) var email: String,
    @Column(name = "password", nullable = false) @JsonBackReference var password: String,
    @Column(name = "register_date", nullable = false) val registerDate: Timestamp,
    @Column(name = "is_admin") val isAdmin: Boolean = false,
    @OneToMany(mappedBy = "publisher", orphanRemoval = true)
    @JsonManagedReference("user-mods")
    val mods: MutableSet<Mod> = mutableSetOf()
) {
  constructor() : this("", "", "", "", Timestamp(System.currentTimeMillis()), false)

  constructor(
      login: String,
      username: String,
      email: String,
      password: String
  ) : this(login, username, email, password, Timestamp(System.currentTimeMillis()), false)
}