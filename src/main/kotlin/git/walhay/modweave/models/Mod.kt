package git.walhay.modweave.models

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*

@Entity
@Table(schema = "modweave", name = "mods")
data class Mod(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    val id: Long? = null,
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
        name = "mods_categories",
        joinColumns = [JoinColumn(name = "mod_id")],
        inverseJoinColumns = [JoinColumn(name = "category_name")])
    @JsonManagedReference
    val categories: Set<Category> = mutableSetOf()
) {
  constructor() : this(null, "", "", User(), Game())
}
