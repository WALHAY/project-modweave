package git.walhay.modweave.api.user.http.dto

import java.time.LocalDateTime

data class UserResponseDto(val name: String, val registerDate: LocalDateTime)
