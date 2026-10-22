package git.walhay.modweave.api.common

import git.walhay.modweave.domain.common.paging.SortDirection
import git.walhay.modweave.domain.common.paging.SortOrder
import org.springframework.data.domain.Sort

/** Reject undeclared properties before JPA and use a unique tie-breaker for stable pagination. */
fun Sort.toSortOrders(vararg properties: String, identity: String = "id"): List<SortOrder> {
  require(all { it.property in properties }) {
    "Unsupported sort property; allowed: ${properties.joinToString()}"
  }
  val stableSort = if (getOrderFor(identity) != null) this else and(Sort.by(identity))
  return stableSort.toList().map {
    SortOrder(
        it.property, if (it.isAscending) SortDirection.ASC else SortDirection.DESC, it.isIgnoreCase)
  }
}
