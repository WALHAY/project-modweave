package git.walhay.modweave.api.config

import git.walhay.modweave.api.config.interceptor.UserActionLoggingInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class RequestLoggingConfiguration(
    private val userActionLoggingInterceptor: UserActionLoggingInterceptor,
) : WebMvcConfigurer {
  override fun addInterceptors(registry: InterceptorRegistry) {
    registry.addInterceptor(userActionLoggingInterceptor).addPathPatterns("/api/**")
  }
}
