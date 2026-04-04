package git.walhay.modweave.api.category

import git.walhay.modweave.api.category.command.CategoryCreateCommand
import git.walhay.modweave.api.category.command.CategoryUpdateCommand

interface ICategoryService {
  fun getCategories(): List<Category>

  fun uploadCategory(command: CategoryCreateCommand): Category

  fun updateCategory(command: CategoryUpdateCommand): Category

  fun deleteCategory(categoryId: CategoryId)
}
