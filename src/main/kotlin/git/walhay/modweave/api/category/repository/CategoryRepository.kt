package git.walhay.modweave.api.category.repository

import git.walhay.modweave.api.category.Category
import git.walhay.modweave.api.category.CategoryId

interface CategoryRepository {
  fun findAll(): List<Category>

  fun findAllByNameIn(categories: Collection<CategoryId>): Set<Category>

  fun existsByNameIgnoreCase(categoryId: CategoryId): Boolean

  fun deleteByNameIgnoreCase(categoryId: CategoryId)

  fun save(category: Category): Category
}
