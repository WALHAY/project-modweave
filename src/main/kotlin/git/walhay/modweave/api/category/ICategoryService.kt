package git.walhay.modweave.api.category

import git.walhay.modweave.api.category.dto.CategoryUpdateDto
import git.walhay.modweave.api.category.dto.CategoryUploadDto

interface ICategoryService {
  fun getCategories(): List<Category>

  fun uploadCategory(dto: CategoryUploadDto): Category

  fun deleteCategory(categoryId: CategoryId)

  fun updateCategory(dto: CategoryUpdateDto): Category
}
