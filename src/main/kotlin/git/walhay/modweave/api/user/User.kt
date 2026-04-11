package git.walhay.modweave.api.user

import git.walhay.modweave.api.mod.Mod
import java.io.Serializable
import java.time.LocalDateTime

data class User(
    val username: UserId,
    var name: String,
    var email: String,
    var password: String,
    val registerDate: LocalDateTime = LocalDateTime.now(),
    var isAdmin: Boolean = false,
    val mods: MutableSet<Mod> = mutableSetOf()
) : Serializable {
  constructor(
      username: String,
      name: String,
      email: String,
      password: String
  ) : this(
      username = UserId(username.lowercase().trim()),
      name = name,
      email = email.lowercase().trim(),
      password = password)
}
