package git.walhay.modweave.model

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(schema = "modweave", name = "mod_versions")
data class Version(
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
    @JsonBackReference("mod-versions")
    val mod: Mod,
    @OneToMany(mappedBy = "version", fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference("version-file")
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
