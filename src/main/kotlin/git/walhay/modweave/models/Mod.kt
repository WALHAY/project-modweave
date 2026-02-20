package git.walhay.modweave.models

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import git.walhay.modweave.utils.spinalCase
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(schema = "modweave", name = "mods")
data class Mod(
    @Id
    @Column(name = "id", nullable = false, length = 50)
    val id: String,

    @Column(name = "name", nullable = false, length = 255)
    val name: String,

    @Column(name = "description", columnDefinition = "text")
    val description: String? = null,

    @Column(name = "image_path", nullable = false, length = 500)
    val imagePath: String,

    @Column(name = "creation_date", nullable = false)
    val creationDate: LocalDateTime,

    @Column(name = "approved", nullable = false)
    val approved: Boolean = false,

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_login", nullable = false)
    @JsonBackReference("user-mods")
    val publisher: User,

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    @JsonBackReference("game-mods")
    val game: Game,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        schema = "modweave",
        name = "mods_categories",
        joinColumns = [JoinColumn(name = "mod_id", nullable = false)],
        inverseJoinColumns = [JoinColumn(name = "category_name", nullable = false)]
    )
    @JsonManagedReference("mods-categories")
    val categories: Set<Category> = emptySet(),

    @OneToMany(mappedBy = "mod", fetch = FetchType.LAZY, orphanRemoval = true, cascade = [CascadeType.ALL])
    @JsonManagedReference("mod-versions")
    val versions: MutableList<Version> = mutableListOf()
) {
    constructor(
        name: String,
        description: String? = null,
        publisher: User,
        game: Game,
        imagePath: String,
        categories: Set<Category> = emptySet(),
        versions: List<Version> = emptyList()
    ) : this(
        id = name.spinalCase(),
        name = name,
        description = description,
        imagePath = imagePath,
        creationDate = LocalDateTime.now(),
        approved = false,
        publisher = publisher,
        game = game,
        categories = categories,
        versions = versions.toMutableList()
    )

    constructor() : this(
        name = "",
        description = null,
        publisher = User(),
        game = Game(),
        imagePath = "",
        categories = emptySet(),
        versions = emptyList()
    )
}
