package git.walhay.modweave.models

import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*

@Entity
@Table(schema = "modweave", name = "games")
data class Game(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    var id: Long? = null,
    @Column(name = "name", nullable = false) val name: String,
    @Column(name = "description") val description: String,
    @OneToMany(mappedBy = "game", orphanRemoval = true)
    @JsonManagedReference("game-mods")
    val mods: MutableList<Mod> = mutableListOf()
) {
  constructor() : this(null, "", "")

  constructor(name: String, description: String) : this(null, name, description)
}
