package git.walhay.modweave.api.mod

@JvmInline
value class ModId(
    val value: String = "",
) {
  override fun toString(): String = value
}
