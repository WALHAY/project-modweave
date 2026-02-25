package git.walhay.modweave.api.file

import git.walhay.modweave.api.file.dto.FileDto
import git.walhay.modweave.api.version.Version
import io.mcarle.konvert.api.KonvertTo
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(schema = "modweave", name = "mod_files")
@EntityListeners(FileEntityListener::class)
@KonvertTo(FileDto::class)
class File(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long? = null,
    @Column(name = "filename", nullable = false) val filename: String,
    @Column(name = "file_path", nullable = false) val filePath: String,
    @Column(name = "downloads") var downloads: Int = 0,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mod_version_id", nullable = false)
    val version: Version,
    @Column(name = "metainfo", nullable = true)
    @JdbcTypeCode(SqlTypes.JSON)
    val metainfo: String? = null
) {
  constructor(
      filename: String,
      filePath: String,
      version: Version,
      metainfo: String? = null
  ) : this(null, filename, filePath, 0, version, metainfo)

  constructor() : this("", "", Version(), null)
}
