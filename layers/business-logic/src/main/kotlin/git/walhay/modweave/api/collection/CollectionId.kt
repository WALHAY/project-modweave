package git.walhay.modweave.api.collection

import java.util.UUID

@JvmInline
value class CollectionId(
    val value: UUID = UUID.randomUUID(),
) {
  override fun toString(): String = value.toString()
}
