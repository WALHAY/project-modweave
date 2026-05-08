package git.walhay.modweave.api.version.repository

import git.walhay.modweave.api.file.repository.FileEntity
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.version.Version
import git.walhay.modweave.api.version.VersionStatus
import io.mcarle.konvert.api.KonvertFrom
import io.mcarle.konvert.api.KonvertTo
import jakarta.persistence.*
import org.hibernate.annotations.ColumnTransformer
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.io.Serializable
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(schema = "modweave", name = "mod_versions")
@KonvertTo(Version::class, mapFunctionName = "toDomain")
@KonvertFrom(Version::class)
class VersionEntity(
    @Id @Column(name = "id") val id: UUID,
    @Column(name = "name", nullable = false) val name: String,
    @Column(name = "changes", columnDefinition = "text") val changes: String? = null,
    @Column(name = "upload_date", nullable = false) val uploadDate: LocalDateTime,
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", columnDefinition = "modweave.version_status")
    @ColumnTransformer(write = "?::modweave.version_status")
    val status: VersionStatus,
    @Column(name = "mod_id", nullable = false) val modId: ModId,
    @OneToMany(
        mappedBy = "versionId",
        fetch = FetchType.LAZY,
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
    )
    val files: MutableList<FileEntity> = mutableListOf(),
) : Serializable {
  constructor(
      name: String,
      changes: String? = null,
      modId: ModId,
  ) : this(
      id = UUID.randomUUID(),
      name = name,
      changes = changes,
      uploadDate = LocalDateTime.now(),
      status = VersionStatus.PENDING,
      modId = modId,
      files = mutableListOf(),
  )

  constructor() : this("", null, ModId(""))

  companion object
}
