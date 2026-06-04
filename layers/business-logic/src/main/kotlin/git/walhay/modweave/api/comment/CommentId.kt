package git.walhay.modweave.api.comment

import java.util.UUID

@JvmInline
value class CommentId(
    val value: UUID = UUID.randomUUID(),
) {
  override fun toString(): String = value.toString()
}
