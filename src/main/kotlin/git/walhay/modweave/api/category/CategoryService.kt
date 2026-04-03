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
      throw CategoryExistsException(dto.name)
    }

    return categoryRepository.save(dto.toCategory())
  }

  override fun deleteCategory(categoryId: CategoryId) {
    if (!categoryRepository.existsByNameIgnoreCase(categoryId)) {
      throw CategoryNotFoundException(categoryId)
    }

    categoryRepository.deleteByNameIgnoreCase(categoryId)
  }

  override fun updateCategory(dto: CategoryUpdateDto): Category {
    if (!categoryRepository.existsByNameIgnoreCase(dto.name)) {
      throw CategoryNotFoundException(dto.name)
    }

    return categoryRepository.save(dto.toCategory())
  }
}
