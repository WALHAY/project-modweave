package git.walhay.modweave.domain.common.paging

data class PageRequest(
    val page: Int,
    val size: Int,
    val sort: List<SortOrder> = emptyList(),
) {
  init {
    require(page >= 0) { "Page number must not be negative" }
    require(size > 0) { "Page size must be positive" }
  }
}

data class SortOrder(
    val property: String,
    val direction: SortDirection = SortDirection.ASC,
    val ignoreCase: Boolean = false,
)

enum class SortDirection {
  ASC,
  DESC,
}
