package git.walhay.modweave.domain.common.paging

import git.walhay.modweave.domain.config.properties.ModweaveProperties
import org.springframework.stereotype.Component

@Component
class PageSizePolicy(
    private val properties: ModweaveProperties,
) {
  fun normalize(size: Int): Int {
    require(size > 0) { "Page size must be positive" }
    return size.coerceAtMost(properties.business.maxPageSize)
  }
}
