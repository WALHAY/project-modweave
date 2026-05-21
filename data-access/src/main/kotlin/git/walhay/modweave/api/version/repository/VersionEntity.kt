package git.walhay.modweave.api.version.repository

import git.walhay.modweave.api.file.repository.FileEntity
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.version.Version
import git.walhay.modweave.api.version.VersionId
import git.walhay.modweave.api.version.VersionStatus
import jakarta.persistence.*
import java.io.Serializable
import java.time.LocalDateTime
import java.util.UUID
import org.hibernate.annotations.ColumnTransformer
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(schema = "modweave", name = "mod_versions")
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
    @Column(name = "mod_id", nullable = false) val modId: String,
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
      modId: String,
  ) : this(
      id = UUID.randomUUID(),
      name = name,
      changes = changes,
      uploadDate = LocalDateTime.now(),
      status = VersionStatus.PENDING,
      modId = modId,
      files = mutableListOf(),
  )

  constructor() : this("", null, "")

  fun toDomain(): Version =
      Version(
          VersionId(id),
          name,
          changes,
          uploadDate,
          status,
          ModId(modId),
          files.map { it.toDomain() }.toMutableList())

  companion object {
    fun fromVersion(version: Version): VersionEntity =
        VersionEntity(
            version.id.value,
            version.name,
            version.changes,
            version.uploadDate,
            version.status,
            version.modId.value,
            version.files.map { FileEntity.fromFile(it) }.toMutableList())
  }
}
