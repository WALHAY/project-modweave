package git.walhay.modweave.api.category.repository

import git.walhay.modweave.api.category.Category
import git.walhay.modweave.api.category.CategoryId
import org.springframework.stereotype.Repository

@Repository
@org.springframework.context.annotation.Profile("postgres")
class JpaCategoryRepository(
    private val repository: SpringDataCategoryRepository,
) : CategoryRepository {
  override fun findAll(): List<Category> = repository.findAll().map { it.toDomain() }

  override fun findAllByNameIn(categories: Collection<CategoryId>): Set<Category> =
      repository.findAllByNameIn(categories.map { it.value }).map { it.toDomain() }.toSet()

  override fun existsByNameIgnoreCase(categoryId: CategoryId): Boolean =
      repository.existsByNameIgnoreCase(categoryId.value)

  override fun deleteByNameIgnoreCase(categoryId: CategoryId) =
      repository.deleteByNameIgnoreCase(categoryId.value)

  override fun save(category: Category): Category =
      repository.save(CategoryEntity.fromCategory(category)).toDomain()
}
