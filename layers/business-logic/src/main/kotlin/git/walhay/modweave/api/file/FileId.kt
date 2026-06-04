package git.walhay.modweave.api.file

import java.util.UUID

@JvmInline
value class FileId(
    val value: UUID = UUID.randomUUID(),
) {
  override fun toString(): String = value.toString()
}
