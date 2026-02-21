package git.walhay.modweave.config

import git.walhay.modweave.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpMethod
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
class SecurityConfiguration @Autowired constructor(private val userRepository: UserRepository) {

  @Bean fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

  @Bean
  fun userDetailsService(): UserDetailsService = UserDetailsService { login ->
    val user =
        userRepository.findByIdOrNull(login.lowercase())
            ?: throw UsernameNotFoundException("User not found: ${login.lowercase()}")

    User.withUsername(user.login)
        .password(user.password)
        .roles(if (user.isAdmin) "ADMIN" else "USER")
        .build()
  }

  @Bean
  fun authenticationManager(auth: AuthenticationConfiguration): AuthenticationManager =
      auth.authenticationManager

  @Bean
  fun filterChain(http: HttpSecurity): SecurityFilterChain {
    http {
      authorizeHttpRequests {
        authorize(HttpMethod.POST, "/api/v1/games", hasRole("ADMIN"))
        authorize(HttpMethod.POST, "/api/v1/users/**", permitAll)
        authorize(HttpMethod.GET, "/api/v1/**", permitAll)
        authorize(anyRequest, authenticated)
      }
      formLogin { loginPage = "/login" }
      csrf { disable() }
      httpBasic {}
    }

    return http.build()
  }
}
