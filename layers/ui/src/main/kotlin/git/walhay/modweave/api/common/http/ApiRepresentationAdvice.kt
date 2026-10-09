package git.walhay.modweave.api.common.http

import git.walhay.modweave.api.category.http.dto.CategoryCollectionDto
import git.walhay.modweave.api.category.http.dto.CategoryResponseDto
import git.walhay.modweave.api.collection.http.dto.CollectionResponseDto
import git.walhay.modweave.api.comment.http.dto.CommentResponseDto
import git.walhay.modweave.api.common.paging.Page
import git.walhay.modweave.api.game.http.dto.GameResponseDto
import git.walhay.modweave.api.mod.http.dto.ModResponseDto
import git.walhay.modweave.api.user.http.dto.UserResponseDto
import git.walhay.modweave.api.version.http.dto.FileResponseDto
import git.walhay.modweave.api.version.http.dto.VersionResponseDto
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.http.server.ServletServerHttpResponse
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice
import org.springframework.web.util.UriComponentsBuilder
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.node.ObjectNode

/** Hypermedia belongs to the HTTP representation; the shared domain and v1 DTOs stay unchanged. */
@RestControllerAdvice
class ApiRepresentationAdvice(private val mapper: ObjectMapper) : ResponseBodyAdvice<Any> {
  private val validators = RepresentationValidators()

  override fun supports(
      returnType: MethodParameter,
      converterType: Class<out HttpMessageConverter<*>>,
  ): Boolean = true

  override fun beforeBodyWrite(
      body: Any?,
      returnType: MethodParameter,
      selectedContentType: MediaType,
      selectedConverterType: Class<out HttpMessageConverter<*>>,
      request: ServerHttpRequest,
      response: ServerHttpResponse,
  ): Any? {
    val servletRequest = (request as? ServletServerHttpRequest)?.servletRequest ?: return body
    val servletResponse = (response as? ServletServerHttpResponse)?.servletResponse ?: return body
    if (!servletRequest.requestURI
        .removePrefix(servletRequest.contextPath)
        .startsWith("/api/v2/") || servletResponse.status !in 200..299 || body == null)
        return body
    val representation = render(body, request) ?: return body
    if (servletRequest.method == "GET" || servletRequest.method == "HEAD") {
      val etag = RepresentationValidators.etag(mapper.writeValueAsBytes(representation))
      val key = request.uri.toString() + ":" + (servletRequest.userPrincipal?.name ?: "guest")
      if (validators.check(servletRequest, servletResponse, key, etag)) return null
    }
    return representation
  }

  private fun render(body: Any, request: ServerHttpRequest): ObjectNode? {
    if (body !is Page<*> &&
        body !is CategoryCollectionDto &&
        body !is UserResponseDto &&
        body !is GameResponseDto &&
        body !is CategoryResponseDto &&
        body !is ModResponseDto &&
        body !is VersionResponseDto &&
        body !is FileResponseDto &&
        body !is CommentResponseDto &&
        body !is CollectionResponseDto)
        return null
    val node = mapper.valueToTree(body) as ObjectNode
    val links = linkedMapOf<String, ApiLink>()
    val context = (request as ServletServerHttpRequest).servletRequest.contextPath
    val auth =
        SecurityContextHolder.getContext().authentication?.takeIf {
          it.isAuthenticated && it !is AnonymousAuthenticationToken
        }
    val admin = auth?.authorities?.any { it.authority == "ROLE_ADMIN" } == true
    fun path(template: String, vararg values: Any): String =
        UriComponentsBuilder.fromPath("$context/api/v2$template")
            .encode()
            .buildAndExpand(*values)
            .toUriString()
    fun link(rel: String, href: String, method: String = "GET") {
      links[rel] = ApiLink(href, method)
    }
    fun listLink(rel: String, href: String) = link(rel, "$href?page=0&size=20")
    fun children(values: List<*>): tools.jackson.databind.node.ArrayNode =
        mapper.createArrayNode().also { array ->
          values.forEach { array.add(render(it!!, request)) }
        }
    when (body) {
      is Page<*> -> {
        node.set("content", children(body.content))
        fun pageLink(rel: String, page: Int) =
            link(
                rel,
                UriComponentsBuilder.fromUri(request.uri)
                    .scheme(null)
                    .host(null)
                    .port(-1)
                    .replaceQueryParam("page", page)
                    .replaceQueryParam("size", body.size)
                    .build(true)
                    .toUriString())
        pageLink("self", body.page)
        pageLink("first", 0)
        pageLink("last", (body.totalPages - 1).coerceAtLeast(0))
        if (body.page > 0)
            pageLink(
                "previous", (body.page - 1).coerceAtMost((body.totalPages - 1).coerceAtLeast(0)))
        if (body.page < body.totalPages - 1) pageLink("next", body.page + 1)
      }
      is CategoryCollectionDto -> {
        node.set("items", children(body.items))
        link("self", path("/categories"))
        if (admin) link("create", path("/categories"), "POST")
      }
      is UserResponseDto -> {
        link("self", path("/users/{id}", body.username))
        listLink("mods", path("/users/{id}/mods", body.username))
        listLink("collections", path("/users/{id}/collections", body.username))
        if (auth?.name == body.username) link("update", path("/users/me"), "PATCH")
      }
      is GameResponseDto -> {
        link("self", path("/games/{id}", body.id))
        listLink("mods", path("/mods"))
        if (admin) link("delete", path("/games/{id}", body.id), "DELETE")
      }
      is CategoryResponseDto -> {
        val self = path("/categories/{name}", body.name)
        link("self", self)
        link("collection", path("/categories"))
        if (admin) {
          link("update", self, "PATCH")
          link("delete", self, "DELETE")
        }
      }
      is ModResponseDto -> {
        link("self", path("/mods/{id}", body.id))
        link("game", path("/games/{id}", body.gameId))
        link("publisher", path("/users/{id}", body.publisherId))
        listLink("versions", path("/mods/{id}/versions", body.id))
        link(
            "comments",
            path("/comments") +
                "?modId=" +
                org.springframework.web.util.UriUtils.encodeQueryParam(body.id, Charsets.UTF_8) +
                "&page=0&size=20")
        if (admin || auth?.name == body.publisherId) {
          link("create-version", path("/mods/{id}/versions", body.id), "POST")
          link("delete", path("/mods/{id}", body.id), "DELETE")
        }
      }
      is VersionResponseDto -> {
        val self = path("/mods/{mod}/versions/{id}", body.modId, body.id)
        link("self", self)
        link("mod", path("/mods/{id}", body.modId))
        link("files", self)
        node.set("files", children(body.files))
        if (admin) {
          link("moderate", self, "PATCH")
          link("delete", self, "DELETE")
        }
      }
      is FileResponseDto -> {
        link("self", path("/files/{id}", body.id))
        link("download", path("/files/{id}", body.id))
      }
      is CommentResponseDto -> {
        link("self", path("/comments/{id}", body.id))
        link("mod", path("/mods/{id}", body.modId))
        link("author", path("/users/{id}", body.authorId))
        if (admin || auth?.name == body.authorId)
            link("delete", path("/comments/{id}", body.id), "DELETE")
      }
      is CollectionResponseDto -> {
        link("self", path("/collections/{id}", body.id))
        listLink("mods", path("/collections/{id}/mods", body.id))
        if (admin || auth?.name == body.ownerId) {
          link("add-mod", path("/collections/{id}/mods", body.id), "POST")
          link("delete", path("/collections/{id}", body.id), "DELETE")
        }
      }
    }
    node.set("_links", mapper.valueToTree(links))
    return node
  }
}

data class ApiLink(val href: String, val method: String = "GET")
