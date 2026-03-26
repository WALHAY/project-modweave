package git.walhay.modweave.api.version.repository

import git.walhay.modweave.api.file.repository.FileEntity
import git.walhay.modweave.api.mod.repository.ModEntity
import git.walhay.modweave.api.version.Version
import io.mcarle.konvert.api.KonvertTo
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(schema = "modweave", name = "mod_versions")
@KonvertTo(Version::class, mapFunctionName = "toModel")
class VersionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Long? = null,
    @Column(name = "name", nullable = false) val name: String,
    @Column(name = "changes", columnDefinition = "text") val changes: String? = null,
    @Column(name = "upload_date", nullable = false) val uploadDate: LocalDateTime,
    @Column(name = "approved", nullable = false) val approved: Boolean = false,
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "mod_id", nullable = false)
    val mod: ModEntity,
    @OneToMany(
        mappedBy = "version",
        fetch = FetchType.LAZY,
        cascade = [CascadeType.ALL],
        orphanRemoval = true)
    val files: MutableList<FileEntity> = mutableListOf()
) {
  constructor(
      name: String,
      changes: String? = null,
      mod: ModEntity
  ) : this(
      name = name,
      changes = changes,
      uploadDate = LocalDateTime.now(),
      approved = false,
      mod = mod,
      files = mutableListOf())

  constructor() : this("", null, ModEntity())
}
