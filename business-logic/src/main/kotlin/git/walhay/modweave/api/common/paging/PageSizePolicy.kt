package git.walhay.modweave.api.common.paging

import git.walhay.modweave.config.properties.ModweaveProperties
import org.springframework.stereotype.Component

@Component
class PageSizePolicy(
    private val properties: ModweaveProperties,
) {
  fun normalize(size: Int): Int = size.coerceAtMost(properties.business.maxPageSize)
}
