package git.walhay.modweave.cli

import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.shell.core.command.annotation.Command
import org.springframework.shell.core.command.annotation.Option
import org.springframework.stereotype.Component

@Component
class AuthShellCommands(
    private val authenticationManager: AuthenticationManager,
    private val authSession: CliAuthSession,
) {
  @Command(name = ["auth", "login"], description = "Authenticate using Basic Auth credentials.")
  fun authLogin(
      @Option(longName = "username") username: String,
      @Option(longName = "password") password: String,
  ): String {
    try {
      val authentication =
          authenticationManager.authenticate(
              UsernamePasswordAuthenticationToken.unauthenticated(username, password),
          )
      authSession.login(authentication.name)
      return "Authenticated as '${authentication.name}'."
    } catch (ex: AuthenticationException) {
      throw IllegalArgumentException("Authentication failed for user '$username'.")
    }
  }

  @Command(name = ["auth", "logout"], description = "Clear current authenticated user.")
  fun authLogout(): String {
    authSession.logout()
    return "Logged out."
  }

  @Command(name = ["auth", "whoami"], description = "Show current authenticated user.")
  fun authWhoami(): String = authSession.currentUsername() ?: "Not authenticated."
}
