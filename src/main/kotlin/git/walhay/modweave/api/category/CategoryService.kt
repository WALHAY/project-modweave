package git.walhay.modweave.api.category

import git.walhay.modweave.api.category.dto.CategoryResponseDto
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

  override fun uploadCategory(dto: CategoryResponseDto): Category {
    if (categoryRepository.existsByNameIgnoreCase(dto.name.lowercase())) {
      throw CategoryExistsException("Category with name=${dto.name.lowercase()} already exists")
    }

    return categoryRepository.save(dto.toCategory())
  }

  override fun deleteCategory(name: String) {
    if (!categoryRepository.existsByNameIgnoreCase(name)) {
      throw CategoryNotFoundException("Category with name=$name not found for deletion")
    }

    categoryRepository.deleteByNameIgnoreCase(name)
  }

  override fun updateCategory(dto: CategoryResponseDto): Category {
    if (!categoryRepository.existsByNameIgnoreCase(dto.name.lowercase())) {
      throw CategoryNotFoundException(
          "Category with name=${dto.name.lowercase()} not found for update")
    }

    return categoryRepository.save(dto.toCategory())
  }
}
