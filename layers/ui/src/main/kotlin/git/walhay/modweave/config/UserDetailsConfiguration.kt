package git.walhay.modweave.config

import git.walhay.modweave.api.user.UserId
import git.walhay.modweave.api.user.UserService
import git.walhay.modweave.api.user.exception.UserNotFoundException
import mu.KLogger
import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException

@Configuration
class UserDetailsConfiguration(
    @Lazy private val userService: UserService,
) {
  private val logger: KLogger = KotlinLogging.logger {}

  @Bean
  fun userDetailsService(): UserDetailsService {
    logger.info { "Initializing UserDetailsService bean" }
    return UserDetailsService { username ->
      logger.debug { "Loading user details for username: $username" }
      val user =
          try {
            userService.findUserByUsername(UserId(username.trim().lowercase()))
          } catch (e: UserNotFoundException) {
            throw UsernameNotFoundException("User not found", e)
          }

      User.withUsername(user.username.value)
          .password(user.password)
          .roles(if (user.isAdmin) "ADMIN" else "USER")
          .build()
    }
  }
}
