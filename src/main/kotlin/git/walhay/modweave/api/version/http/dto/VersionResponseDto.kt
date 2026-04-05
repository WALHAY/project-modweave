package git.walhay.modweave.api.version.http.dto

import git.walhay.modweave.api.version.Version
import io.mcarle.konvert.api.KonvertFrom
import java.time.LocalDateTime

@KonvertFrom(Version::class)
data class VersionResponseDto(
    val name: String,
    val changes: String?,
    val uploadDate: LocalDateTime,
) {
  companion object
}
