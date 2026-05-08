package git.walhay.modweave.api.file.repository

import git.walhay.modweave.api.file.File
import io.mcarle.konvert.api.KonvertFrom
import io.mcarle.konvert.api.KonvertTo
import jakarta.persistence.*
import java.io.Serializable
import org.hibernate.annotations.NaturalId
import java.util.UUID

@Entity
@Table(schema = "modweave", name = "mod_files")
@KonvertTo(File::class, mapFunctionName = "toDomain")
@KonvertFrom(File::class)
class FileEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id") val id: Long,
    @Column(name = "filename", nullable = false) val filename: String,
    @NaturalId @Column(name = "file_path", nullable = false) val filePath: String,
    @Column(name = "downloads") var downloads: Int = 0,
    @Column(name = "mod_version_id", nullable = false) val versionId: UUID,
) : Serializable {
  constructor(
      filename: String,
      filePath: String,
      versionId: UUID,
  ) : this(0, filename, filePath, 0, versionId)

  constructor() : this("", "", UUID.randomUUID())

  companion object
}
