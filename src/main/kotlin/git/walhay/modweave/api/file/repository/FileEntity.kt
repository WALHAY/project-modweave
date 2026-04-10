package git.walhay.modweave.api.file.repository

import git.walhay.modweave.api.file.File
import io.mcarle.konvert.api.KonvertFrom
import io.mcarle.konvert.api.KonvertTo
import jakarta.persistence.*
import org.hibernate.annotations.NaturalId

@Entity
@Table(schema = "modweave", name = "mod_files")
@KonvertTo(File::class, mapFunctionName = "toDomain")
@KonvertFrom(File::class)
class FileEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id") val id: Long,
    @Column(name = "filename", nullable = false) val filename: String,
    @NaturalId @Column(name = "file_path", nullable = false) val filePath: String,
    @Column(name = "downloads") var downloads: Int = 0,
    @Column(name = "mod_version_id", nullable = false) val versionId: Long
) {
  constructor(
      filename: String,
      filePath: String,
      versionId: Long
  ) : this(0, filename, filePath, 0, versionId)

  constructor() : this("", "", 0)

  companion object
}
