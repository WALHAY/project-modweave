package git.walhay.modweave.api.common.sorting

class Sort {
  private val entries: MutableList<SortEntry> = mutableListOf()

  enum class Direction {
    ASC,
    DESC,
  }

  class SortEntry(
      val property: String,
      val direction: Direction,
  )

  fun toSpringSort(): org.springframework.data.domain.Sort =
      org.springframework.data.domain.Sort.by(
          entries.map { entry ->
            when (entry.direction) {
              Direction.ASC -> {
                org.springframework.data.domain.Sort.Order.asc(entry.property)
              }

              Direction.DESC -> {
                org.springframework.data.domain.Sort.Order.desc(entry.property)
              }
            }
          },
      )
}
