package git.walhay.modweave.api.collection

@JvmInline
value class CollectionId(
    val value: Long = 0,
) {
  override fun toString(): String = value.toString()
}
