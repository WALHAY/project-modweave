package git.walhay.modweave.api.category

import git.walhay.modweave.api.category.dto.CategoryResponseDto

interface ICategoryService {
  fun getCategories(): List<Category>

  fun uploadCategory(dto: CategoryResponseDto): Category

  fun deleteCategory(name: String)

  fun updateCategory(dto: CategoryResponseDto): Category
}
