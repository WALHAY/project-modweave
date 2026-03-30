package git.walhay.modweave.api.user

import java.util.*

@JvmInline
value class UserId(val value: UUID = UUID.randomUUID()) {
  override fun toString(): String = value.toString()
}
