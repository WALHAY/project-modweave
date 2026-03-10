package git.walhay.modweave.api.mod.repository

import git.walhay.modweave.api.category.Category
import git.walhay.modweave.api.game.repository.GameEntity
import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.api.user.repository.UserEntity
import git.walhay.modweave.api.version.Version
import git.walhay.modweave.util.spinalCase
import io.mcarle.konvert.api.KonvertTo
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(schema = "modweave", name = "mods")
@KonvertTo(Mod::class, mapFunctionName = "toModel")
class ModEntity(
    @Id @Column(name = "id", nullable = false, length = 50) val id: String,
    @Column(name = "name", nullable = false, length = 255) val name: String,
    @Column(name = "description", columnDefinition = "text") val description: String? = null,
    @Column(name = "image_path", nullable = false, length = 500) val imagePath: String,
    @Column(name = "creation_date", nullable = false) val creationDate: LocalDateTime,
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "publisher_login", nullable = false)
	val publisher: UserEntity,
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "game_id", nullable = false)
	var game: GameEntity,
    @ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
		schema = "modweave",
		name = "mods_categories",
		joinColumns = [JoinColumn(name = "mod_id", nullable = false)],
		inverseJoinColumns = [JoinColumn(name = "category_name", nullable = false)]
	)
	val categories: Set<Category> = emptySet(),
    @OneToMany(
		mappedBy = "mod", fetch = FetchType.LAZY, orphanRemoval = true, cascade = [CascadeType.ALL]
	)
	val versions: MutableList<Version> = mutableListOf()
) {
	constructor() : this("", null, UserEntity(), GameEntity(), "")

	constructor(
        name: String,
        description: String? = null,
        publisher: UserEntity,
        game: GameEntity,
        imagePath: String,
        categories: Set<Category> = emptySet(),
        versions: List<Version> = emptyList()
	) : this(
		id = name.spinalCase(),
		name = name,
		description = description,
		imagePath = imagePath,
		creationDate = LocalDateTime.now(),
		publisher = publisher,
		game= game,
		categories = categories,
		versions = versions.toMutableList()
	)
}