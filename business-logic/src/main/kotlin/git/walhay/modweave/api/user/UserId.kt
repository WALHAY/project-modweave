package git.walhay.modweave.api.user

import java.io.Serializable

@JvmInline
value class UserId(
    val value: String = "",
) : Serializable {
  override fun toString(): String = value
}
