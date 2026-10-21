package git.walhay.modweave.api.common

import git.walhay.modweave.domain.common.paging.Page
import java.net.URI
import org.springframework.hateoas.Link
import org.springframework.web.util.UriComponentsBuilder

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
