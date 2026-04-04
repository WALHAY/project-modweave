package git.walhay.modweave.api.category

import git.walhay.modweave.api.category.command.CategoryCreateCommand
import git.walhay.modweave.api.category.command.CategoryUpdateCommand
import git.walhay.modweave.api.category.exception.CategoryExistsException
import git.walhay.modweave.api.category.exception.CategoryNotFoundException
import git.walhay.modweave.api.category.repository.CategoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CategoryService(val categoryRepository: CategoryRepository) : ICategoryService {

  override fun getCategories(): List<Category> = categoryRepository.findAll()

  override fun uploadCategory(command: CategoryCreateCommand): Category {
    if (categoryRepository.existsByNameIgnoreCase(command.name)) {
      throw CategoryExistsException(command.name)
    }

    return command
        .let { (name, description) -> Category(name, description) }
        .also { categoryRepository.save(it) }
  }

  override fun updateCategory(command: CategoryUpdateCommand): Category {
    if (!categoryRepository.existsByNameIgnoreCase(command.name)) {
      throw CategoryNotFoundException(command.name)
    }

    return command
        .let { (name, description) -> Category(name, description) }
        .also { categoryRepository.save(it) }
  }

  override fun deleteCategory(categoryId: CategoryId) {
    if (!categoryRepository.existsByNameIgnoreCase(categoryId)) {
      throw CategoryNotFoundException(categoryId)
    }

    categoryRepository.deleteByNameIgnoreCase(categoryId)
  }
}
