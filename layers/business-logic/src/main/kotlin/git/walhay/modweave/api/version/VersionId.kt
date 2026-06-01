package git.walhay.modweave.api.version

import java.util.UUID

@JvmInline
value class VersionId(
    val value: UUID = UUID.randomUUID(),
) {
  override fun toString(): String = value.toString()
}
