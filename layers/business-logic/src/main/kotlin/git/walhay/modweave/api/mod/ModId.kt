package git.walhay.modweave.api.mod

import java.io.Serializable

@JvmInline
value class ModId(
    val value: String = "",
) : Serializable {
  override fun toString(): String = value
}
