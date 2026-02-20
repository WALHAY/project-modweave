package git.walhay.modweave.models

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import git.walhay.modweave.utils.spinalCaseWithDots
import jakarta.persistence.*
import java.sql.Date

@Entity
@Table(schema = "modweave", name = "mod_versions")
data class Version(
    @Id @Column("id") var id: String? = null,
    @Column("name") var name: String,
    @Column("changes") var changes: String,
    @Column("upload_date") val uploadDate: Date,
    @ManyToOne(optional = false)
    @JoinColumn(name = "mod_id", nullable = false)
    @JsonBackReference("mod-version")
    val mod: Mod,
    @OneToMany(mappedBy = "version", orphanRemoval = true)
    @JsonManagedReference("version-file")
    val files: List<File> = mutableListOf()
) {
  constructor() : this(null, "", "", Date(System.currentTimeMillis()), Mod())
    constructor(name: String, changes: String, mod: Mod) : this(name.spinalCaseWithDots(), name, changes, Date(System.currentTimeMillis()), mod)
}

