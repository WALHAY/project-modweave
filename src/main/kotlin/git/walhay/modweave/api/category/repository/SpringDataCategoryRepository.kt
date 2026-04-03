package git.walhay.modweave.api.category.repository

import git.walhay.modweave.api.category.CategoryId
import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataCategoryRepository : JpaRepository<CategoryEntity, CategoryId> {
  fun findAllByNameIn(categories: Collection<CategoryId>): Set<CategoryEntity>

  fun existsByNameIgnoreCase(categoryId: CategoryId): Boolean

  fun deleteByNameIgnoreCase(categoryId: CategoryId)
}
