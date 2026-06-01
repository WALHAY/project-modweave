package git.walhay.modweave.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KLogger
import mu.KotlinLogging
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userDetailsService: UserDetailsService,
) : OncePerRequestFilter() {
  private val log: KLogger = KotlinLogging.logger {}

  override fun doFilterInternal(
      request: HttpServletRequest,
      response: HttpServletResponse,
      filterChain: FilterChain,
  ) {
    val authHeader = request.getHeader("Authorization")
    if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response)
      return
    }

    val token = authHeader.removePrefix("Bearer ").trim()
    val username =
        runCatching { jwtService.extractUsername(token) }
            .onFailure { log.debug { "Failed to parse JWT: ${it.message}" } }
            .getOrNull()
            ?: run {
              filterChain.doFilter(request, response)
              return
            }

    if (SecurityContextHolder.getContext().authentication == null) {
      val userDetails = userDetailsService.loadUserByUsername(username)
      val isValid =
          runCatching { jwtService.isAccessTokenValid(token, userDetails) }
              .onFailure { log.debug { "Invalid access token: ${it.message}" } }
              .getOrDefault(false)

      if (isValid) {
        val authorities =
            jwtService
                .extractRoles(token)
                .map { SimpleGrantedAuthority(it) }
                .ifEmpty { userDetails.authorities }
        val authentication = UsernamePasswordAuthenticationToken(userDetails, null, authorities)
        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
        SecurityContextHolder.getContext().authentication = authentication
      }
    }

    filterChain.doFilter(request, response)
  }
}
