package git.walhay.modweave.models

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import git.walhay.modweave.utils.spinalCase
import jakarta.persistence.*

@Entity
@Table(schema = "modweave", name = "mods")
data class Mod(
    @Id
    @Column(name = "id")
    val id: String?,
    @Column(name = "name") val name: String,
    @Column(name = "description") val description: String,
    @ManyToOne(optional = false)
    @JoinColumn(name = "publisher_login", nullable = false)
    @JsonBackReference("user-mods")
    val publisher: User,
    @ManyToOne(optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    @JsonBackReference("game-mods")
    val game: Game,
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        schema = "modweave",
        name = "mods_categories",
        joinColumns = [JoinColumn(name = "mod_id")],
        inverseJoinColumns = [JoinColumn(name = "category_name")])
    @JsonManagedReference("mods-categories")
    val categories: Set<Category> = mutableSetOf(),
    @OneToMany(mappedBy = "mod", orphanRemoval = true)
    @JsonManagedReference("mod-version")
    val versions: List<Version> = mutableListOf()
) {
    constructor(name: String, description: String, publisher: User, game: Game, categories: Set<Category>, versions: List<Version>) : this(name.spinalCase(), name, description, publisher, game, categories, versions)
  constructor() : this(null, "", "", User(), Game())
}
