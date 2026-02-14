package git.walhay.modweave.mods

import com.fasterxml.jackson.annotation.JsonBackReference
import git.walhay.modweave.games.Game
import git.walhay.modweave.user.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

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
    @JsonBackReference
    val publisher: User,
    @ManyToOne(optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    @JsonBackReference
    val game: Game
) {
  constructor() : this(null, "", "", User(), Game())
}
