package git.walhay.modweave.config

import mu.KLogger
import mu.KotlinLogging
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfiguration(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
) {
  private val logger: KLogger = KotlinLogging.logger {}

  @Bean
  fun jwtFilterRegistration(): FilterRegistrationBean<JwtAuthenticationFilter> =
      FilterRegistrationBean(jwtAuthenticationFilter).apply { isEnabled = false }

  @Bean
  fun passwordEncoder(): PasswordEncoder {
    logger.info { "Initializing BCryptPasswordEncoder bean" }
    return BCryptPasswordEncoder()
  }

  @Bean
  fun authenticationManager(auth: AuthenticationConfiguration): AuthenticationManager {
    logger.info { "Initializing AuthenticationManager bean" }
    return auth.authenticationManager
  }

  @Bean
  fun filterChain(http: HttpSecurity): SecurityFilterChain {
    logger.info { "Configuring security filter chain" }
    http {
      csrf { disable() }
      sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
      exceptionHandling {
        authenticationEntryPoint =
            org.springframework.security.web.AuthenticationEntryPoint { _, response, _ ->
              response.sendError(401)
            }
      }
      authorizeHttpRequests {
        authorize("/api/*/auth/**", permitAll)
        authorize(HttpMethod.POST, "/api/*/users", permitAll)
        authorize(HttpMethod.GET, "/api/**", permitAll)
        authorize("/error", permitAll)
        authorize(anyRequest, authenticated)
      }
    }
    http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

    logger.info { "Security filter chain configured successfully" }
    return http.build()
  }
}
