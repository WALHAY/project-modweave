package git.walhay.modweave.models

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*

@Entity
@Table(schema = "modweave", name = "categories")
data class Category(
    @Id
    @Column(name = "name")
    private val name: String,

    @Column(name = "description")
    private val description: String,

    @ManyToMany(mappedBy = "categories")
    @JsonBackReference("mods-categories")
    val categories: Set<Mod> = mutableSetOf()
)