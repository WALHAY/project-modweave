package git.walhay.modweave.api.category.repository

import git.walhay.modweave.api.category.Category

interface CategoryRepository {
  fun findAll(): List<Category>

  fun findAllByNameIn(categories: Collection<String>): Set<Category>

  fun existsByNameIgnoreCase(name: String): Boolean

  fun deleteByNameIgnoreCase(name: String)

  fun save(category: Category): Category
}
