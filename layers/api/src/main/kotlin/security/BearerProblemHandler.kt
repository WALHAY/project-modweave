package git.walhay.modweave.api.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.net.URI
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper

/** Retains Spring's bearer challenges and adds the same error representation as MVC. */
@Component
class BearerProblemHandler(private val mapper: JsonMapper) :
    AuthenticationEntryPoint, AccessDeniedHandler {
  private val entryPoint = BearerTokenAuthenticationEntryPoint()
  private val deniedHandler = BearerTokenAccessDeniedHandler()

  override fun commence(
      request: HttpServletRequest,
      response: HttpServletResponse,
      error: AuthenticationException,
  ) {
    entryPoint.commence(request, response, error)
    writeProblem(request, response, "Authentication is required or the bearer token is invalid")
  }

  override fun handle(
      request: HttpServletRequest,
      response: HttpServletResponse,
      error: AccessDeniedException,
  ) {
    deniedHandler.handle(request, response, error)
    writeProblem(request, response, "Access denied")
  }

  private fun writeProblem(
      request: HttpServletRequest,
      response: HttpServletResponse,
      detail: String,
  ) {
    val problem = ProblemDetail.forStatusAndDetail(HttpStatus.valueOf(response.status), detail)
    problem.instance = URI.create(request.requestURI)
    response.contentType = MediaType.APPLICATION_PROBLEM_JSON_VALUE
    mapper.writeValue(response.outputStream, problem)
  }
}
