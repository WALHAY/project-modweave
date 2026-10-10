package git.walhay.modweave.api.version

import git.walhay.modweave.domain.version.VersionStatus

data class VersionModerationDto(val status: Decision) {
  enum class Decision(val versionStatus: VersionStatus) {
    APPROVED(VersionStatus.APPROVED),
    REJECTED(VersionStatus.REJECTED),
  }
}
