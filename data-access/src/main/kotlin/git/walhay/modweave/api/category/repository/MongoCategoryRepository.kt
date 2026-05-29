package git.walhay.modweave.api.category.repository

import git.walhay.modweave.api.category.Category
import git.walhay.modweave.api.category.CategoryId
import org.springframework.context.annotation.Profile
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Document(collection = "categories")
data class CategoryDocument(
    @Id val name: String,
    val description: String? = null,
)

interface SpringDataMongoCategoryRepository : MongoRepository<CategoryDocument, String> {
  fun findAllByNameIn(names: Collection<String>): List<CategoryDocument>

  fun existsByNameIgnoreCase(name: String): Boolean

  fun deleteByNameIgnoreCase(name: String)
}

@Repository
@Profile("mongodb")
class MongoCategoryRepository(private val repository: SpringDataMongoCategoryRepository) :
    git.walhay.modweave.api.category.repository.CategoryRepository {
  private fun CategoryDocument.toDomain(): Category =
      Category(CategoryId(this.name), this.description)

  private fun Category.toDocument(): CategoryDocument =
      CategoryDocument(this.name.value, this.description)

  override fun findAll(): List<Category> = repository.findAll().map { it.toDomain() }

  override fun findAllByNameIn(categories: Collection<CategoryId>): Set<Category> =
      repository.findAllByNameIn(categories.map { it.value }).map { it.toDomain() }.toSet()

  override fun existsByNameIgnoreCase(categoryId: CategoryId): Boolean =
      repository.existsByNameIgnoreCase(categoryId.value)

  override fun deleteByNameIgnoreCase(categoryId: CategoryId) =
      repository.deleteByNameIgnoreCase(categoryId.value)

  override fun save(category: Category): Category =
      repository.save(category.toDocument()).toDomain()
}
