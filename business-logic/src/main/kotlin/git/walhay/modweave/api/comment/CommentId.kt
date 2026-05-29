package git.walhay.modweave.api.comment

import java.io.Serializable

@JvmInline
value class CommentId(
    val value: Long = 0,
) : Serializable {
  override fun toString(): String = value.toString()
}
