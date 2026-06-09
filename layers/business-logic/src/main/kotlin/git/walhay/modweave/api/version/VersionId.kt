package git.walhay.modweave.api.version

import java.io.Serializable
import java.util.UUID

@JvmInline
value class VersionId(
    val value: UUID = UUID.randomUUID(),
) : Serializable {
  override fun toString(): String = value.toString()
}
