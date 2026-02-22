package git.walhay.modweave.model

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(schema = "modweave", name = "mod_files")
data class File(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long? = null,
    @Column(name = "filename", nullable = false) val filename: String,
    @Column(name = "file_path", nullable = false) val filePath: String,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mod_version_id", nullable = false)
    @JsonBackReference("version-file")
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
  ) : this(null, filename, filePath, version, metainfo)

  constructor() : this("", "", Version(), null)
}
