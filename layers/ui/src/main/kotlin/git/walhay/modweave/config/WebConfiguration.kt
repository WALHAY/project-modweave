package git.walhay.modweave.config

import mu.KLogger
import mu.KotlinLogging
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.method.HandlerTypePredicate
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfiguration : WebMvcConfigurer {
  private val logger: KLogger = KotlinLogging.logger {}

  override fun configureApiVersioning(configurer: ApiVersionConfigurer) {
    logger.info { "Configuring API versions 1 and 2" }
    configurer.addSupportedVersions("1", "2").setDefaultVersion("1").usePathSegment(1)
  }

  override fun configurePathMatch(config: PathMatchConfigurer) {
    logger.info { "Configuring path prefix for REST controllers" }
    config.addPathPrefix(
        "/api/{version}", HandlerTypePredicate.forAnnotation(RestController::class.java))
    logger.info { "Path configuration completed" }
  }

  override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
    registry
        .addResourceHandler("/api/v2/swagger-ui/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/5.33.1/")
  }
}

// A regular Controller keeps documentation outside the versioned business-controller prefix.
@Controller
class ApiDocumentationController {
  @GetMapping("/api/v2/docs", produces = ["text/html"])
  @ResponseBody
  fun documentation(): String =
      """
      <!doctype html>
      <html lang="en"><head><meta charset="utf-8"><title>ModWeave API v2</title>
      <meta name="viewport" content="width=device-width, initial-scale=1">
      <link rel="stylesheet" href="/api/v2/swagger-ui/swagger-ui.css"></head>
      <body><div id="swagger-ui"></div>
      <script src="/api/v2/swagger-ui/swagger-ui-bundle.js"></script>
      <script>
      SwaggerUIBundle({url: '/api/v2/openapi.yaml', dom_id: '#swagger-ui',
        deepLinking: true, validatorUrl: null});
      </script></body></html>
      """
          .trimIndent()
}
