package git.walhay.modweave.api.user

@JvmInline
value class UserId(val value: String = "") {
  override fun toString(): String = value
}
