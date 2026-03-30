package git.walhay.modweave.api.user.dto

import java.time.LocalDateTime
import java.util.*

data class UserDto(val id: UUID, val username: String, val registerDate: LocalDateTime)
