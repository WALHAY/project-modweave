package git.walhay.modweave.api.common.paging

import git.walhay.modweave.api.common.sorting.Sort
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable

data class PageRequest(val page: Int = 0, val size: Int = 20, val sort: Sort? = null) {
  init {
    require(page >= 0) { "page must be >= 0" }
    require(size > 0) { "size must be > 0" }
  }

  fun toSpringPageable(): Pageable {
    if (sort != null) {
      return PageRequest.of(page, size, sort.toSpringSort())
    }
    return PageRequest.of(page, size)
  }
}
