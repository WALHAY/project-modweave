package git.walhay.modweave.games

import com.fasterxml.jackson.annotation.JsonManagedReference
import git.walhay.modweave.mods.Mod
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

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
