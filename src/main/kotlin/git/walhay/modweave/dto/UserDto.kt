package git.walhay.modweave.dto

import java.time.LocalDateTime

data class UserDto(val username: String, val registerDate: LocalDateTime, val mods: List<ModDto>)
