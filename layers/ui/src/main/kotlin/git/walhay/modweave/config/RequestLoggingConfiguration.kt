package git.walhay.modweave.config

import git.walhay.modweave.config.interceptor.UserActionLoggingInterceptor
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
