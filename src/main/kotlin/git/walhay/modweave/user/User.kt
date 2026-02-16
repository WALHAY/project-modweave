package git.walhay.modweave.user

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import git.walhay.modweave.mods.Mod
import jakarta.persistence.*
import java.sql.Date

@Entity(name = "users")
@Table(schema = "modweave", name = "users")
data class User(
    @Id @Column(name = "login") val login: String,
    @Column(name = "username", unique = true, nullable = false) val username: String,
    @Column(name = "email", unique = true, nullable = false) val email: String,
    @Column(name = "passhash", nullable = false) @JsonBackReference val passhash: String,
    @Column(name = "register_date", nullable = false) val register_date: Date,
    @Column(name = "is_admin") val is_admin: Boolean = false,
    @OneToMany(mappedBy = "publisher", orphanRemoval = true)
    @JsonManagedReference("user-mods")
    val mods: MutableSet<Mod> = mutableSetOf()
) {
  constructor() : this("", "", "", "", Date(System.currentTimeMillis()), false)

  constructor(
      login: String,
      username: String,
      email: String,
      passhash: String
  ) : this(login, username, email, passhash, Date(System.currentTimeMillis()), false)
}
