package git.walhay.modweave.api.user

import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.api.user.dto.UserDto
import git.walhay.modweave.api.user.repository.UserEntity
import io.mcarle.konvert.api.KonvertTo
import java.time.LocalDateTime

@KonvertTo(UserEntity::class, mapFunctionName = "toEntity")
@KonvertTo(UserDto::class)
data class User(
    val login: String,
    var username: String,
    var email: String,
    var password: String,
    val registerDate: LocalDateTime = LocalDateTime.now(),
    var isAdmin: Boolean = false,
    val mods: MutableSet<Mod> = mutableSetOf()
) {
    constructor() : this("", "", "", "")
}