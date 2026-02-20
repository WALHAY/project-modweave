package git.walhay.modweave.model

import com.fasterxml.jackson.annotation.JsonManagedReference
import git.walhay.modweave.util.spinalCase
import jakarta.persistence.*

@Entity
@Table(schema = "modweave", name = "games")
data class Game(
    @Id
    @Column(name = "id", nullable = false)
    val id: String,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "description", columnDefinition = "text")
    val description: String? = null,

    @Column(name = "image_path", nullable = false)
    val imagePath: String,

    @OneToMany(mappedBy = "game", fetch = FetchType.LAZY)
    @JsonManagedReference("game-mods")
    val mods: MutableList<Mod> = mutableListOf()
) {
    constructor(name: String, description: String? = null, imagePath: String) : this(
        id = name.spinalCase(),
        name = name,
        description = description,
        imagePath = imagePath
    )

    constructor() : this("", null, "")
}
