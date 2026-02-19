package git.walhay.modweave.models

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*
import java.sql.Date

@Entity
@Table(schema = "modweave", name = "mod_versions")
data class Version (
    @Id
    @Column("version_name")
    var versionName: String,
    @Column("changes")
    var changes: String,
    @Column("upload_date")
    val uploadDate: Date,
    @Column("bucket_key")
    val bucketKey: String,
    @ManyToOne(optional = false)
    @JoinColumn(name = "mod_id", nullable = false)
    @JsonBackReference("mod-version")
    val mod: Mod,
    @OneToMany(mappedBy = "version", orphanRemoval = true)
    @JsonManagedReference("version-file")
    val files: List<File> = mutableListOf()
) {
   constructor() : this("", "", Date(System.currentTimeMillis()), "", Mod())
}