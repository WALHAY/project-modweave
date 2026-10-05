package git.walhay.modweave.api.common.paging

data class Page<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
) {
  val isEmpty: Boolean
    get() = content.isEmpty()

  inline fun <R> map(transform: (T) -> R): Page<R> =
      Page(content.map(transform), page, size, totalElements, totalPages)
}
