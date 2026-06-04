package git.walhay.modweave.api.file.repository

import git.walhay.modweave.api.file.File
import git.walhay.modweave.api.file.FileId
import git.walhay.modweave.api.version.VersionId
import jakarta.persistence.*
import java.io.Serializable
import java.util.UUID
import org.hibernate.annotations.NaturalId

@Entity
@Table(schema = "modweave", name = "mod_files")
class FileEntity(
    @Id @Column(name = "id") val id: UUID,
    @Column(name = "filename", nullable = false) val filename: String,
    @NaturalId @Column(name = "file_path", nullable = false) val filePath: String,
    @Column(name = "downloads") var downloads: Int = 0,
    @Column(name = "mod_version_id", nullable = false) val versionId: UUID,
) : Serializable {
  constructor(
      filename: String,
      filePath: String,
      versionId: UUID,
  ) : this(UUID.randomUUID(), filename, filePath, 0, versionId)

  constructor() : this("", "", UUID.randomUUID())

  fun toDomain(): File = File(FileId(id), filename, filePath, downloads, VersionId(versionId))

  companion object {
    fun fromFile(file: File): FileEntity =
        FileEntity(
            file.id.value, file.filename, file.filePath, file.downloads, file.versionId.value)
  }
}
