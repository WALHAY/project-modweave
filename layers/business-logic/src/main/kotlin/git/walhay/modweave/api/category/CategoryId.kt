package git.walhay.modweave.api.category

import java.io.Serializable

@JvmInline
value class CategoryId(val value: String = "") : Serializable {
  override fun toString(): String = value
}
