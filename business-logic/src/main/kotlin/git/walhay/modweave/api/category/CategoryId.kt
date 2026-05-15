package git.walhay.modweave.api.category

@JvmInline
value class CategoryId(val value: String = "") {
  override fun toString(): String = value
}
