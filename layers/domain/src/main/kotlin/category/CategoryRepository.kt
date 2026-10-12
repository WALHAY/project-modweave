package git.walhay.modweave.domain.category

interface CategoryRepository {
  fun findAll(): List<Category>

  fun findAllByNameIn(categories: Collection<CategoryId>): Set<Category>

  fun existsByNameIgnoreCase(categoryId: CategoryId): Boolean

  fun deleteByNameIgnoreCase(categoryId: CategoryId)

  fun save(category: Category): Category
}
