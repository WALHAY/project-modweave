package git.walhay.modweave.persistence.common.paging

import git.walhay.modweave.domain.common.paging.Page
import git.walhay.modweave.domain.common.paging.PageRequest
import git.walhay.modweave.domain.common.paging.SortDirection
import org.springframework.data.domain.Page as SpringPage
import org.springframework.data.domain.PageRequest as SpringPageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

fun PageRequest.toSpringPageable(): Pageable =
    SpringPageRequest.of(
        page,
        size,
        Sort.by(
            sort.map { order ->
              Sort.Order(
                      if (order.direction == SortDirection.ASC) Sort.Direction.ASC
                      else Sort.Direction.DESC,
                      order.property)
                  .let { if (order.ignoreCase) it.ignoreCase() else it }
            }))

inline fun <T : Any, R> SpringPage<T>.toDomainPage(transform: (T) -> R): Page<R> =
    Page(
        content = content.map(transform),
        page = number,
        size = size,
        totalElements = totalElements,
        totalPages = totalPages,
    )
