package git.walhay.modweave.security

import git.walhay.modweave.user.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig @Autowired constructor(private val userRepository: UserRepository) {

  @Bean fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

  @Bean
  fun userDetailsService(): UserDetailsService = UserDetailsService { login ->
    val user =
        userRepository.findByIdOrNull(login)
            ?: throw UsernameNotFoundException("User not found: $login")

    User.withUsername(user.login)
        .password(user.passhash)
        .roles(if (user.is_admin) "ADMIN" else "USER")
        .build()
  }

  @Bean
  fun authenticationManager(auth: AuthenticationConfiguration): AuthenticationManager =
      auth.authenticationManager

  @Bean
  fun filterChain(http: HttpSecurity): SecurityFilterChain {
    http {
      authorizeHttpRequests {
        authorize("/", permitAll)
        authorize("/mods", authenticated)
        authorize("/register", permitAll)
          authorize(anyRequest, authenticated)
      }
      formLogin { loginPage = "/login" }
      csrf { disable() }
      httpBasic {}
    }

    return http.build()
  }
}
