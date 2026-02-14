package git.walhay.modweave.user

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import git.walhay.modweave.mods.Mod
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.sql.Date

@Entity(name = "users")
@Table(schema = "modweave", name = "users")
data class User(
    @Id @Column(name = "login") val login: String,
    @Column(name = "nickname", unique = true, nullable = false) val username: String,
    @Column(name = "email", unique = true, nullable = false) val email: String,
    @Column(name = "passhash", nullable = false) @JsonBackReference val passhash: String,
    @Column(name = "register_date", nullable = false) val register_date: Date,
    @Column(name = "is_admin") val is_admin: Boolean = false,
    @OneToMany(mappedBy = "publisher", orphanRemoval = true)
    @JsonManagedReference
    val mods: MutableSet<Mod> = mutableSetOf()
) {
  constructor() : this("", "", "", "", Date(System.currentTimeMillis()), false)
}
