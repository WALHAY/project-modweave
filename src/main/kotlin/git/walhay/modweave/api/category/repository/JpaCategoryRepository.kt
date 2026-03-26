package git.walhay.modweave.api.category.repository

import git.walhay.modweave.api.category.Category
import org.springframework.stereotype.Repository

@Repository
class JpaCategoryRepository(private val repository: SpringDataCategoryRepository) : CategoryRepository {
    override fun findAll(): List<Category> = repository.findAll().map { it.toModel() }

    override fun findAllByNameIn(categories: Collection<String>): Set<Category> = emptySet()

    override fun existsByNameIgnoreCase(name: String): Boolean = repository.existsByNameIgnoreCase(name)

    override fun deleteByNameIgnoreCase(name: String) = repository.deleteByNameIgnoreCase(name)

    override fun save(category: Category): Category = repository.save<CategoryEntity>(category.toEntity()).toModel()
}