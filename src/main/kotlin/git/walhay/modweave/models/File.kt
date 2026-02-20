package git.walhay.modweave.models

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*

@Entity
@Table(schema = "modweave", name = "mod_files")
data class File(
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column("file_key") val fileKey: String,
    @ManyToOne @JoinColumn("mod_version") @JsonBackReference("version-file") val version: Version
) {
  constructor() : this(null, "", Version())
}
