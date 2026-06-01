package git.walhay.modweave.api.auth.http

import git.walhay.modweave.api.auth.http.dto.AuthRequestDto
import git.walhay.modweave.api.auth.http.dto.RefreshTokenRequestDto
import git.walhay.modweave.api.auth.http.dto.TokenResponseDto
import git.walhay.modweave.config.JwtService
import jakarta.validation.Valid
import mu.KLogger
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetailsService
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

  @PostMapping("/login")
  fun login(
      @Valid @ModelAttribute dto: AuthRequestDto,
  ): TokenResponseDto {
    logger.info { "POST /auth/login - authenticating user: ${dto.username}" }
    val authentication =
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(dto.username, dto.password))
    val userDetails =
        authentication.principal as org.springframework.security.core.userdetails.UserDetails
    val accessToken = jwtService.generateAccessToken(userDetails)
    val refreshToken = jwtService.generateRefreshToken(userDetails)
    return TokenResponseDto(accessToken, refreshToken)
  }

  @PostMapping("/refresh")
  fun refresh(
      @Valid @ModelAttribute dto: RefreshTokenRequestDto,
  ): TokenResponseDto {
    logger.info { "POST /auth/refresh - refreshing token" }
    val username = jwtService.extractUsername(dto.refreshToken)
    val userDetails = userDetailsService.loadUserByUsername(username)
    if (!jwtService.isRefreshTokenValid(dto.refreshToken, userDetails)) {
      throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token")
    }
    val accessToken = jwtService.generateAccessToken(userDetails)
    val refreshToken = jwtService.generateRefreshToken(userDetails)
    return TokenResponseDto(accessToken, refreshToken)
  }
}
