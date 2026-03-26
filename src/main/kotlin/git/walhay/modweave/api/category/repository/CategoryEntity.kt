package git.walhay.modweave.api.category.repository

import git.walhay.modweave.api.category.Category
import git.walhay.modweave.api.mod.repository.ModEntity
import io.mcarle.konvert.api.KonvertTo
import jakarta.persistence.*

@Entity
@Table(schema = "modweave", name = "categories")
@KonvertTo(Category::class, mapFunctionName = "toModel")
class CategoryEntity(
    @Id @Column(name = "name", nullable = false) var name: String,
    @Column(name = "description", columnDefinition = "text") var description: String? = null,
    @ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY)
    val mods: Set<ModEntity> = emptySet()
) {
  constructor() : this("")

  @PrePersist
  @PreUpdate
  fun normalize() {
    name.trim()
    description?.trim()
  }
}
