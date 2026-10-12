package git.walhay.modweave.domain.mod

import java.io.Serializable

@JvmInline
value class ModId(
    val value: String = "",
) : Serializable {
  override fun toString(): String = value
}
