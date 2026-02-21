package git.walhay.modweave.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.method.HandlerTypePredicate
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfiguration : WebMvcConfigurer{

    override fun configureApiVersioning(configurer: ApiVersionConfigurer) {
        configurer.addSupportedVersions("1").setDefaultVersion("1").usePathSegment(1)
    }

    override fun configurePathMatch(config: PathMatchConfigurer) {
        config.addPathPrefix(
            "/api/{version}",
            HandlerTypePredicate.forAnnotation(RestController::class.java)
        )
    }
}