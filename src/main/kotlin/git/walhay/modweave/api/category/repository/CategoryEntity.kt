package git.walhay.modweave.api.category.repository

import git.walhay.modweave.api.category.Category
import git.walhay.modweave.api.category.CategoryId
import git.walhay.modweave.api.mod.repository.ModEntity
import io.mcarle.konvert.api.KonvertTo
import jakarta.persistence.*

@Entity
@Table(schema = "modweave", name = "categories")
@KonvertTo(Category::class, mapFunctionName = "toModel")
@KonvertTo(CategoryId::class)
class CategoryEntity(
    @Id @Column(name = "name", nullable = false) var name: CategoryId,
    @Column(name = "description", columnDefinition = "text") var description: String? = null,
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        schema = "modweave",
        name = "mods_categories",
        joinColumns = [JoinColumn("category_name")],
        inverseJoinColumns = [JoinColumn(name = "mod_id")])
    val mods: Set<ModEntity> = emptySet()
) {
  constructor() : this(CategoryId())

  @PrePersist
  @PreUpdate
  fun normalize() {
    name = CategoryId(name.value.trim())
    description = description?.trim()
  }
}
