package git.walhay.modweave.api.file

@JvmInline
value class FileId(private val value: Long = 0) {
  override fun toString(): String = value.toString()
}
