package git.walhay.modweave.api.version

import git.walhay.modweave.api.file.File
import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.api.version.dto.VersionDto
import io.mcarle.konvert.api.KonvertTo
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(schema = "modweave", name = "mod_versions")
@KonvertTo(VersionDto::class)
class Version(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Long? = null,
    @Column(name = "name", nullable = false) val name: String,
    @Column(name = "changes", columnDefinition = "text") val changes: String? = null,
    @Column(name = "upload_date", nullable = false) val uploadDate: LocalDateTime,
    @Column(name = "downloads") var downloads: Int = 0,
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "mod_id", nullable = false)
    val mod: Mod,
    @OneToMany(mappedBy = "version", fetch = FetchType.LAZY, orphanRemoval = true)
    val files: MutableList<File> = mutableListOf()
) {
  constructor(
      name: String,
      changes: String? = null,
      mod: Mod
  ) : this(
      name = name,
      changes = changes,
      uploadDate = LocalDateTime.now(),
      downloads = 0,
      mod = mod,
      files = mutableListOf())

  constructor() : this("", null, Mod())
}
