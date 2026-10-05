package git.walhay.modweave.api.common.paging

import org.springframework.data.domain.Page as SpringPage

inline fun <T : Any, R> SpringPage<T>.toDomainPage(transform: (T) -> R): Page<R> =
    Page(
        content = content.map(transform),
        page = number,
        size = size,
        totalElements = totalElements,
        totalPages = totalPages,
    )
