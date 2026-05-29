package git.walhay.modweave.api.category

import git.walhay.modweave.api.category.command.CategoryCreateCommand
import git.walhay.modweave.api.category.command.CategoryUpdateCommand
import git.walhay.modweave.api.category.exception.CategoryExistsException
import git.walhay.modweave.api.category.exception.CategoryNotFoundException
import git.walhay.modweave.api.category.repository.CategoryRepository
import mu.KLogger
import mu.KotlinLogging
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CategoryService(
    val categoryRepository: CategoryRepository,
) : ICategoryService {
  private val logger: KLogger = KotlinLogging.logger {}

  @Cacheable("categories")
  override fun getCategories(): List<Category> {
    logger.debug { "Fetching all categories" }
    return categoryRepository.findAll()
  }

  @CacheEvict(value = ["categories"], allEntries = true)
  @PreAuthorize("hasRole('ADMIN')")
  override fun uploadCategory(command: CategoryCreateCommand): Category {
    logger.info { "Creating new category: ${command.name}" }
    if (categoryRepository.existsByNameIgnoreCase(command.name)) {
      logger.warn { "Category creation failed - category already exists: ${command.name}" }
      throw CategoryExistsException(command.name)
    }

    val category =
        command
            .let { (name, description) -> Category(name, description) }
            .also { categoryRepository.save(it) }

    logger.info { "Category created successfully: ${category.name}" }
    return category
  }

  @CacheEvict(value = ["categories"], allEntries = true)
  @PreAuthorize("hasRole('ADMIN')")
  override fun updateCategory(command: CategoryUpdateCommand): Category {
    logger.info { "Updating category: ${command.name}" }
    if (!categoryRepository.existsByNameIgnoreCase(command.name)) {
      logger.warn { "Category update failed - category not found: ${command.name}" }
      throw CategoryNotFoundException(command.name)
    }

    val category =
        command
            .let { (name, description) -> Category(name, description) }
            .let { categoryRepository.save(it) }

    logger.info { "Category updated successfully: ${category.name}" }
    return category
  }

  @CacheEvict(value = ["categories"], allEntries = true)
  @PreAuthorize("hasRole('ADMIN')")
  override fun deleteCategory(categoryId: CategoryId) {
    logger.info { "Deleting category: $categoryId" }
    if (!categoryRepository.existsByNameIgnoreCase(categoryId)) {
      logger.warn { "Category deletion failed - category not found: $categoryId" }
      throw CategoryNotFoundException(categoryId)
    }

    categoryRepository.deleteByNameIgnoreCase(categoryId)
    logger.info { "Category deleted successfully: $categoryId" }
  }
}
