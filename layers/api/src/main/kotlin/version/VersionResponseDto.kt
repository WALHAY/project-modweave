package git.walhay.modweave.api.version

import git.walhay.modweave.api.common.ApiLink
import git.walhay.modweave.api.common.ApiLinkContext
import git.walhay.modweave.api.common.ApiResource
import git.walhay.modweave.domain.version.Version
import java.time.LocalDateTime
import java.util.UUID

data class VersionResponseDto(
    val id: UUID,
    val files: List<FileResponseDto>,
    val name: String,
    val changes: String?,
    val uploadDate: LocalDateTime,
    val status: String,
    val modId: String,
    override val links: Map<String, ApiLink> = emptyMap(),
) : ApiResource {
  override fun withLinks(context: ApiLinkContext): VersionResponseDto =
      with(context) {
        copy(
            files = files.map { it.withLinks(context) },
            links =
                buildMap {
                  val self = link("/mods/{mod}/versions/{id}", modId, id)
                  put("self", self)
                  put("mod", link("/mods/{id}", modId))
                  put("files", self)
                  if (isAdmin) {
                    put("moderate", self.copy(method = "PATCH"))
                    put("delete", self.copy(method = "DELETE"))
                  }
                })
      }

  companion object {
    fun fromVersion(version: Version): VersionResponseDto =
        VersionResponseDto(
            id = version.id.value,
            files = version.files.map { FileResponseDto(it.id.value, it.filename, it.downloads) },
            name = version.name,
            changes = version.changes,
            uploadDate = version.uploadDate,
            status = version.status.toString(),
            modId = version.modId.value)
  }
}

data class FileResponseDto(
    val id: UUID,
    val filename: String,
    val downloads: Int,
    override val links: Map<String, ApiLink> = emptyMap(),
) : ApiResource {
  override fun withLinks(context: ApiLinkContext): FileResponseDto =
      with(context) {
        val self = link("/files/{id}", id)
        copy(links = mapOf("self" to self, "download" to self))
      }
}
