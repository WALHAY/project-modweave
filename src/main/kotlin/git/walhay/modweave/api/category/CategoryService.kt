package git.walhay.modweave.api.category

import git.walhay.modweave.api.category.dto.CategoryUpdateDto
import git.walhay.modweave.api.category.dto.CategoryUploadDto
import git.walhay.modweave.api.category.dto.toCategory
import git.walhay.modweave.api.category.exception.CategoryExistsException
import git.walhay.modweave.api.category.exception.CategoryNotFoundException
import git.walhay.modweave.api.category.repository.CategoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CategoryService(val categoryRepository: CategoryRepository) : ICategoryService {

  override fun getCategories(): List<Category> = categoryRepository.findAll()

  override fun uploadCategory(dto: CategoryUploadDto): Category {
    if (categoryRepository.existsByNameIgnoreCase(dto.name)) {
      throw CategoryExistsException("Category with name=${dto.name} already exists")
    }

    return categoryRepository.save(dto.toCategory())
  }

  override fun deleteCategory(categoryId: CategoryId) {
    if (!categoryRepository.existsByNameIgnoreCase(categoryId)) {
      throw CategoryNotFoundException("Category with name=$categoryId not found for deletion")
    }

    categoryRepository.deleteByNameIgnoreCase(categoryId)
  }

  override fun updateCategory(dto: CategoryUpdateDto): Category {
    if (!categoryRepository.existsByNameIgnoreCase(dto.name)) {
      throw CategoryNotFoundException("Category with name=${dto.name} not found for update")
    }

    return categoryRepository.save(dto.toCategory())
  }
}
