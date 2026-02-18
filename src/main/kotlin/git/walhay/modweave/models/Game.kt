package git.walhay.modweave.models

import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*

@Entity
@Table(schema = "modweave", name = "games")
data class Game(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    val id: Long? = null,
    @Column(name = "name") val name: String,
    @Column(name = "description") val description: String,
    @OneToMany(mappedBy = "game", orphanRemoval = true)
    @JsonManagedReference("game-mods")
    val mods: MutableList<Mod> = mutableListOf()
) {
  constructor() : this(null, "", "")
}
