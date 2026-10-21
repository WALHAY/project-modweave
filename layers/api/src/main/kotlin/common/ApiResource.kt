package git.walhay.modweave.api.common

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import git.walhay.modweave.domain.common.paging.Page
import java.net.URI
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.Link
import org.springframework.hateoas.Links
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import org.springframework.web.util.UriComponentsBuilder

/** Marks response DTOs that have a hypermedia representation in v2. */
interface ApiResource

data class ApiLink(@get:JsonIgnore val link: Link, val method: String) {
  val href: String
    get() = link.href
}

/** EntityModel unwraps the payload; this adapter preserves OpenAPI's href/method links. */
class ApiModel(resource: Any, @get:JsonProperty("_links") val resourceLinks: Map<String, ApiLink>) :
    EntityModel<Any>(resource, resourceLinks.map { (rel, link) -> link.link.withRel(rel) }) {
  @JsonIgnore override fun getLinks(): Links = super.getLinks()
}

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

/** Preserve the contract's flat pagination metadata. */
data class PageResponseDto<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val empty: Boolean,
) {
  companion object {
    fun from(page: Page<*>, uri: URI, assembler: ApiResourceAssembler): ApiModel {
      fun link(number: Int) =
          ApiLink(
              Link.of(
                  UriComponentsBuilder.fromPath(uri.rawPath)
                      .query(uri.rawQuery)
                      .replaceQueryParam("page", number)
                      .replaceQueryParam("size", page.size)
                      .build(true)
                      .toUriString()),
              "GET")
      val last = (page.totalPages - 1).coerceAtLeast(0)
      val links = buildMap {
        put("self", link(page.page))
        put("first", link(0))
        put("last", link(last))
        if (page.page > 0) put("previous", link((page.page - 1).coerceAtMost(last)))
        if (page.page < last) put("next", link(page.page + 1))
      }
      return ApiModel(
          PageResponseDto(
              page.content.map { assembler.toModel(it as ApiResource) },
              page.page,
              page.size,
              page.totalElements,
              page.totalPages,
              page.isEmpty),
          links)
    }
  }
}
