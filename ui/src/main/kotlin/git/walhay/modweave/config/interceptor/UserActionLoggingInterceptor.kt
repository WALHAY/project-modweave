package git.walhay.modweave.config.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KLogger
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class UserActionLoggingInterceptor(
    private val logger: KLogger = KotlinLogging.logger {},
) : HandlerInterceptor {
  companion object {
    private const val START_TIME_ATTR = "requestStartTimeMillis"
  }

  override fun preHandle(
      request: HttpServletRequest,
      response: HttpServletResponse,
      handler: Any,
  ): Boolean {
    request.setAttribute(START_TIME_ATTR, System.currentTimeMillis())
    return true
  }

  override fun afterCompletion(
      request: HttpServletRequest,
      response: HttpServletResponse,
      handler: Any,
      ex: Exception?,
  ) {
    val startedAt = request.getAttribute(START_TIME_ATTR) as? Long ?: System.currentTimeMillis()
    val elapsedMillis = System.currentTimeMillis() - startedAt
    val user = request.userPrincipal?.name ?: "anonymous"
    logger.info {
      "userAction user=$user method=${request.method} uri=${request.requestURI} status=${response.status} durationMs=$elapsedMillis"
    }
  }
}
