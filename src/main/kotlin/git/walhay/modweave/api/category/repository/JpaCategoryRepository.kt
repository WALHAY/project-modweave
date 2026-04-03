package git.walhay.modweave.api.category.repository

import git.walhay.modweave.api.category.Category
import git.walhay.modweave.api.category.CategoryId
import git.walhay.modweave.api.category.toEntity
import org.springframework.stereotype.Repository

@Repository
class JpaCategoryRepository(private val repository: SpringDataCategoryRepository) :
    CategoryRepository {
  override fun findAll(): List<Category> = repository.findAll().map { it.toModel() }

  override fun findAllByNameIn(categories: Collection<CategoryId>): Set<Category> =
      repository.findAllByNameIn(categories).map { it.toModel() }.toSet()

  override fun existsByNameIgnoreCase(categoryId: CategoryId): Boolean =
      repository.existsByNameIgnoreCase(categoryId)

  override fun deleteByNameIgnoreCase(categoryId: CategoryId) =
      repository.deleteByNameIgnoreCase(categoryId)

  override fun save(category: Category): Category = repository.save(category.toEntity()).toModel()
}
