package git.walhay.modweave.api.file

@JvmInline
value class FileId(val value: Long = 0) {
  override fun toString(): String = value.toString()
}
