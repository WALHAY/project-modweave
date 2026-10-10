package git.walhay.modweave.api.common

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import git.walhay.modweave.domain.common.paging.Page
import java.net.URI
import org.springframework.web.util.UriComponentsBuilder
import org.springframework.web.util.UriUtils

/** DTOs own their links; empty links are omitted in legacy v1 responses. */
interface ApiResource {
  @get:JsonProperty("_links")
  @get:JsonInclude(JsonInclude.Include.NON_EMPTY)
  val links: Map<String, ApiLink>

  fun withLinks(context: ApiLinkContext): ApiResource
}

data class ApiLink(val href: String, val method: String = "GET")

class ApiLinkContext(
    private val contextPath: String,
    private val username: String?,
    val isAdmin: Boolean,
) {
  fun isSelf(owner: String): Boolean = username != null && username == owner

  fun canManage(owner: String): Boolean = isAdmin || isSelf(owner)

  fun link(template: String, vararg values: Any, method: String = "GET"): ApiLink =
      ApiLink(
          UriComponentsBuilder.fromPath("$contextPath/api/v2$template")
              .encode()
              .buildAndExpand(*values)
              .toUriString(),
          method)

  fun collection(
      template: String,
      vararg values: Any,
      query: Map<String, Any> = emptyMap()
  ): ApiLink {
    val uri =
        UriComponentsBuilder.fromUriString(link(template, *values).href)
            .queryParam("page", 0)
            .queryParam("size", 20)
    query.forEach { (name, value) ->
      uri.queryParam(name, UriUtils.encode(value.toString(), Charsets.UTF_8))
    }
    return ApiLink(uri.build(true).toUriString())
  }
}

data class PageResponseDto<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val empty: Boolean,
    @get:JsonProperty("_links") val links: Map<String, ApiLink>,
) {
  companion object {
    fun from(page: Page<*>, uri: URI, context: ApiLinkContext): PageResponseDto<*> {
      fun link(number: Int) =
          ApiLink(
              UriComponentsBuilder.fromPath(uri.rawPath)
                  .query(uri.rawQuery)
                  .replaceQueryParam("page", number)
                  .replaceQueryParam("size", page.size)
                  .build(true)
                  .toUriString())
      val last = (page.totalPages - 1).coerceAtLeast(0)
      val links = buildMap {
        put("self", link(page.page))
        put("first", link(0))
        put("last", link(last))
        if (page.page > 0) put("previous", link((page.page - 1).coerceAtMost(last)))
        if (page.page < last) put("next", link(page.page + 1))
      }
      return PageResponseDto(
          page.content.map { if (it is ApiResource) it.withLinks(context) else it },
          page.page,
          page.size,
          page.totalElements,
          page.totalPages,
          page.isEmpty,
          links)
    }
  }
}
