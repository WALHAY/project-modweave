package git.walhay.modweave.domain.version

import java.io.Serializable
import java.util.UUID

@JvmInline
value class VersionId(
    val value: UUID = UUID.randomUUID(),
) : Serializable {
  override fun toString(): String = value.toString()
}
