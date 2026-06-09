package git.walhay.modweave.api.file

import java.io.Serializable
import java.util.UUID

@JvmInline
value class FileId(
    val value: UUID = UUID.randomUUID(),
) : Serializable {
  override fun toString(): String = value.toString()
}
