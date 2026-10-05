package git.walhay.modweave.api.category.repository

import git.walhay.modweave.api.category.Category
import git.walhay.modweave.api.category.CategoryId
import jakarta.persistence.*
import java.io.Serializable

@Entity
@Table(schema = "modweave", name = "categories")
class CategoryEntity(
    @Id @Column(name = "name", nullable = false) var name: String,
    @Column(name = "description", columnDefinition = "text") var description: String? = null,
) : Serializable {
  constructor() : this("")

  @PrePersist
  @PreUpdate
  fun normalize() {
    name = name.trim()
    description = description?.trim()
  }

  fun toDomain(): Category = Category(CategoryId(name), description)

  companion object {
    fun fromCategory(category: Category): CategoryEntity =
        CategoryEntity(category.name.value, category.description)
  }
}
