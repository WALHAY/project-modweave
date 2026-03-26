package git.walhay.modweave.api.category

import git.walhay.modweave.api.category.dto.CategoryDto

interface ICategoryService {
  fun getCategories(): List<Category>

  fun uploadCategory(dto: CategoryDto): Category

  fun deleteCategory(name: String)

  fun updateCategory(dto: CategoryDto): Category
}
