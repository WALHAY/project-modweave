package git.walhay.modweave.api.collection

import java.io.Serializable
import java.util.UUID

@JvmInline
value class CollectionId(
    val value: UUID = UUID.randomUUID(),
) : Serializable {
  override fun toString(): String = value.toString()
}
