package git.walhay.modweave.model

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*

@Entity
@Table(schema = "modweave", name = "categories")
data class Category(
    @Id @Column(name = "name", nullable = false) val name: String,
    @Column(name = "description", columnDefinition = "text") val description: String? = null,
    @ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY)
    @JsonBackReference("mods-categories")
    val mods: Set<Mod> = emptySet()
) {
  constructor() : this("")
}
