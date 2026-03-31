package git.walhay.modweave.api.category

@JvmInline
value class CategoryName(val name: String) {
  override fun toString(): String = name
}
