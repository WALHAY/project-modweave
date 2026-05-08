package git.walhay.modweave.config

import mu.KLogger
import mu.KotlinLogging
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.method.HandlerTypePredicate
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfiguration : WebMvcConfigurer {
  private val logger: KLogger = KotlinLogging.logger {}

  override fun configureApiVersioning(configurer: ApiVersionConfigurer) {
    logger.info { "Configuring API versioning with default version 1" }
    configurer.addSupportedVersions("1").setDefaultVersion("1").usePathSegment(1)
  }

  override fun configurePathMatch(config: PathMatchConfigurer) {
    logger.info { "Configuring path prefix for REST controllers" }
    config.addPathPrefix(
        "/api/{version}", HandlerTypePredicate.forAnnotation(RestController::class.java))
    logger.info { "Path configuration completed" }
  }
}
