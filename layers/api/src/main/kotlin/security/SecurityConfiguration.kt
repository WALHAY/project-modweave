package git.walhay.modweave.api.security

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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfiguration(
    private val jwtAuthenticationConverter: CurrentUserJwtConverter,
    private val jwtDecoder: org.springframework.security.oauth2.jwt.JwtDecoder,
    private val bearerProblemHandler: BearerProblemHandler,
) {
  @Bean fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

  @Bean
  fun authenticationManager(auth: AuthenticationConfiguration): AuthenticationManager =
      auth.authenticationManager

  @Bean
  fun filterChain(http: HttpSecurity): SecurityFilterChain {
    http {
      csrf { disable() }
      sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
      exceptionHandling {
        authenticationEntryPoint = bearerProblemHandler
        accessDeniedHandler = bearerProblemHandler
      }
      authorizeHttpRequests {
        authorize("/api/*/auth/**", permitAll)
        authorize(HttpMethod.POST, "/api/*/users", permitAll)
        authorize(HttpMethod.GET, "/api/**", permitAll)
        authorize(HttpMethod.HEAD, "/api/**", permitAll)
        authorize("/error", permitAll)
        authorize(anyRequest, authenticated)
      }
    }
    http.oauth2ResourceServer { server ->
      server
          .authenticationEntryPoint(bearerProblemHandler)
          .accessDeniedHandler(bearerProblemHandler)
      server.jwt { jwt ->
        jwt.decoder(jwtDecoder).jwtAuthenticationConverter(jwtAuthenticationConverter)
      }
    }

    return http.build()
  }
}
