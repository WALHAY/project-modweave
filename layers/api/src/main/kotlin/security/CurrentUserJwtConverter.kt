package git.walhay.modweave.api.security

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.AccountStatusUserDetailsChecker
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException
import org.springframework.stereotype.Component

/** Keep UserDetails principals and current account permissions for both API versions. */
@Component
class CurrentUserJwtConverter(private val users: UserDetailsService) :
    Converter<Jwt, AbstractAuthenticationToken> {
  override fun convert(jwt: Jwt): AbstractAuthenticationToken {
    val user =
        try {
          users.loadUserByUsername(jwt.subject).also { AccountStatusUserDetailsChecker().check(it) }
        } catch (error: AuthenticationException) {
          throw InvalidBearerTokenException("Account is unavailable", error)
        }
    return UsernamePasswordAuthenticationToken.authenticated(user, null, user.authorities)
  }
}
