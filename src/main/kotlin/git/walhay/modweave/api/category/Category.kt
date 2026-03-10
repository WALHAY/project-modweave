package git.walhay.modweave.api.category

import git.walhay.modweave.api.category.dto.CategoryDto
import git.walhay.modweave.api.mod.repository.ModEntity
import io.mcarle.konvert.api.KonvertTo
import jakarta.persistence.*

@Entity
@Table(schema = "modweave", name = "categories")
@KonvertTo(CategoryDto::class)
class Category(
	@Id @Column(name = "name", nullable = false) var name: String,
	@Column(name = "description", columnDefinition = "text") var description: String? = null,
	@ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY) val mods: Set<ModEntity> = emptySet()
) {
	constructor() : this("")

	@PrePersist
	@PreUpdate
	fun normalize() {
		name.trim()
		description?.trim()
	}
}
