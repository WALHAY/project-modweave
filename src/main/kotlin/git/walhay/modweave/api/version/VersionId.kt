package git.walhay.modweave.api.version

@JvmInline
value class VersionId(
    val value: Long = 0,
) {
  override fun toString(): String = value.toString()
}
