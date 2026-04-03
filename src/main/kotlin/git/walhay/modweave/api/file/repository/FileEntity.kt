package git.walhay.modweave.api.file.repository

import git.walhay.modweave.api.file.File
import git.walhay.modweave.api.file.FileEntityListener
import git.walhay.modweave.api.file.FileId
import git.walhay.modweave.api.version.VersionId
import io.mcarle.konvert.api.KonvertTo
import jakarta.persistence.*
import org.hibernate.annotations.NaturalId

@Entity
@Table(schema = "modweave", name = "mod_files")
@EntityListeners(FileEntityListener::class)
@KonvertTo(File::class, mapFunctionName = "toModel")
class FileEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id") val id: FileId,
    @Column(name = "filename", nullable = false) val filename: String,
    @NaturalId @Column(name = "file_path", nullable = false) val filePath: String,
    @Column(name = "downloads") var downloads: Int = 0,
    @Column(name = "mod_version_id", nullable = false) val versionId: VersionId
) {
  constructor(
      filename: String,
      filePath: String,
      versionId: VersionId
  ) : this(FileId(), filename, filePath, 0, versionId)

  constructor() : this("", "", VersionId())
}
