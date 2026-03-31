package git.walhay.modweave.api.version

@JvmInline
value class VersionId(val value: Long) {
  override fun toString(): String = value.toString()
}
