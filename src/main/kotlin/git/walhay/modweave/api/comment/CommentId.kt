package git.walhay.modweave.api.comment

@JvmInline
value class CommentId(val value: Long = 0) {
  override fun toString(): String = value.toString()
}
