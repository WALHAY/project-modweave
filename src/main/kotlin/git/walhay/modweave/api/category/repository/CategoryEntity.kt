package git.walhay.modweave.api.category.repository

import git.walhay.modweave.api.category.Category
import git.walhay.modweave.api.mod.repository.ModEntity
import io.mcarle.konvert.api.KonvertFrom
import io.mcarle.konvert.api.KonvertTo
import jakarta.persistence.*
import java.io.Serializable

@Entity
@Table(schema = "modweave", name = "categories")
@KonvertTo(Category::class, mapFunctionName = "toDomain")
@KonvertFrom(Category::class)
class CategoryEntity(
    @Id @Column(name = "name", nullable = false) var name: String,
    @Column(name = "description", columnDefinition = "text") var description: String? = null,
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        schema = "modweave",
        name = "mods_categories",
        joinColumns = [JoinColumn("category_name")],
        inverseJoinColumns = [JoinColumn(name = "mod_id")],
    )
    val mods: Set<ModEntity> = emptySet(),
) : Serializable {
  constructor() : this("")

  @PrePersist
  @PreUpdate
  fun normalize() {
    name = name.trim()
    description = description?.trim()
  }

  companion object
}
