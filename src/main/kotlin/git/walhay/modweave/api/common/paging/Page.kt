package git.walhay.modweave.api.common.paging

data class Page<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
) {
    val isEmpty: Boolean get() = content.isEmpty()

    inline fun map(transform: (T) -> T): Page<T> {
        return copy(content = content.map(transform))
    }
}
