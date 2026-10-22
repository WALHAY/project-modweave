package git.walhay.modweave.api.common

import git.walhay.modweave.domain.common.paging.Page
import org.springframework.core.MethodParameter
import org.springframework.core.ResolvableType
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.http.server.ServletServerHttpResponse
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice
import tools.jackson.databind.ObjectMapper

@RestControllerAdvice(basePackages = ["git.walhay.modweave.api"])
class ApiRepresentationAdvice(
    private val mapper: ObjectMapper,
    private val assembler: ApiResourceAssembler,
) : ResponseBodyAdvice<Any> {
  override fun supports(
      returnType: MethodParameter,
      converterType: Class<out HttpMessageConverter<*>>
  ): Boolean {
    val type = ResolvableType.forMethodParameter(returnType)
    val bodyType =
        if (ResponseEntity::class.java.isAssignableFrom(returnType.parameterType))
            type.getGeneric(0).resolve()
        else type.resolve()
    return bodyType != null &&
        (ApiResource::class.java.isAssignableFrom(bodyType) ||
            Page::class.java.isAssignableFrom(bodyType))
  }

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
        .startsWith("/api/v2/") || servletResponse.status !in 200..299)
        return body
    val representation =
        when (body) {
          is ApiResource -> assembler.toModel(body)
          is Page<*> -> PageResponseDto.from(body, request.uri, assembler)
          else -> return body
        }
    if (servletRequest.method in setOf("GET", "HEAD")) {
      val etag = HttpCache.etag(mapper.writeValueAsBytes(representation))
      if (HttpCache.check(servletRequest, servletResponse, etag)) return null
    }
    return representation
  }
}
