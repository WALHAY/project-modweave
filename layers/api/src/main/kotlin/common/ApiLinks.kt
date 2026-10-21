package git.walhay.modweave.api.common

import java.net.URI
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.hateoas.Link
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

/** Read paths and HTTP methods from controllers; apply MVC's global v2 prefix once. */
fun apiLink(handler: KFunction<*>, vararg arguments: Any?): ApiLink {
  val method = requireNotNull(handler.javaMethod)
  // HATEOAS accepts null for non-URI parameters, despite its non-null Java vararg annotation.
  @Suppress("UNCHECKED_CAST")
  val parameters = Array<Any?>(method.parameterCount) { arguments.getOrNull(it) } as Array<Any>
  val link = WebMvcLinkBuilder.linkTo(method, *parameters).withSelfRel().expand()
  val uri = URI.create(link.href)
  val contextPath = ServletUriComponentsBuilder.fromCurrentContextPath().build().path.orEmpty()
  val href =
      "$contextPath/api/v2${uri.rawPath.removePrefix(contextPath)}" +
          (uri.rawQuery?.let { "?$it" } ?: "")
  val mapping =
      requireNotNull(AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping::class.java))
  return ApiLink(Link.of(href), mapping.method.first().name)
}
