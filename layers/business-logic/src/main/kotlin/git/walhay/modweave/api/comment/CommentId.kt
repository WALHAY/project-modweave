package git.walhay.modweave.api.comment

import java.io.Serializable
import java.util.UUID

@JvmInline
value class CommentId(
    val value: UUID = UUID.randomUUID(),
) : Serializable {
  override fun toString(): String = value.toString()
}
