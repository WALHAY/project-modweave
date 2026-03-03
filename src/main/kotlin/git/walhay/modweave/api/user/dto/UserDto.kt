package git.walhay.modweave.api.user.dto

import git.walhay.modweave.api.mod.dto.ModDto
import java.time.LocalDateTime

data class UserDto(val username: String, val registerDate: LocalDateTime, val mods: List<ModDto>)
