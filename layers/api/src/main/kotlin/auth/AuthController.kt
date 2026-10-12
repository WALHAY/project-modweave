package git.walhay.modweave.api.auth

import git.walhay.modweave.api.security.JwtService
import jakarta.validation.Valid
import mu.KLogger
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AccountStatusUserDetailsChecker
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val userDetailsService: UserDetailsService,
    private val jwtService: JwtService,
) {
  private val logger: KLogger = KotlinLogging.logger {}

  @PostMapping("/login", consumes = ["application/x-www-form-urlencoded"])
  fun login(
      @Valid @ModelAttribute dto: AuthRequestDto,
  ): TokenResponseDto {
    logger.info { "POST /auth/login - authenticating user: ${dto.username}" }
    val authentication =
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(dto.username, dto.password))
    return createTokenResponse(authentication.principal as UserDetails)
  }

  @PostMapping("/refresh", consumes = ["application/x-www-form-urlencoded"])
  fun refresh(
      @Valid @ModelAttribute dto: RefreshTokenRequestDto,
  ): TokenResponseDto {
    logger.info { "POST /auth/refresh - refreshing token" }
    val userDetails =
        try {
          val username = jwtService.extractRefreshSubject(dto.refreshToken)
          userDetailsService.loadUserByUsername(username).also {
            AccountStatusUserDetailsChecker().check(it)
          }
        } catch (e: JwtException) {
          throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token", e)
        } catch (e: IllegalArgumentException) {
          throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token", e)
        } catch (e: AuthenticationException) {
          throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token", e)
        }
    return createTokenResponse(userDetails)
  }

  private fun createTokenResponse(
      userDetails: UserDetails,
  ): TokenResponseDto =
      TokenResponseDto(
          accessToken = jwtService.generateAccessToken(userDetails),
          refreshToken = jwtService.generateRefreshToken(userDetails))
}
