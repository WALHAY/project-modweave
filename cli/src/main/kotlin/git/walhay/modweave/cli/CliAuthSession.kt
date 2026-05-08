package git.walhay.modweave.cli

import git.walhay.modweave.api.user.UserId
import org.springframework.stereotype.Component

@Component
class CliAuthSession {
  @Volatile private var authenticatedUsername: String? = null

  fun login(username: String) {
    authenticatedUsername = username
  }

  fun logout() {
    authenticatedUsername = null
  }

  fun currentUsername(): String? = authenticatedUsername

  fun resolveUserId(explicitUserId: String?): UserId {
    val providedUserId = explicitUserId?.trim()
    val resolvedUserId =
        if (providedUserId.isNullOrBlank()) {
          requireNotNull(authenticatedUsername) {
            "No authenticated user. Run 'auth login --username <username> --password <password>' or provide --user-id."
          }
        } else {
          providedUserId
        }
    return UserId(resolvedUserId)
  }
}
