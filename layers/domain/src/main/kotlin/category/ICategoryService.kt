package git.walhay.modweave.domain.category

interface ICategoryService {
  fun getCategories(): List<Category>

  fun uploadCategory(command: CategoryCreateCommand): Category

  fun updateCategory(command: CategoryUpdateCommand): Category

  fun deleteCategory(categoryId: CategoryId)
}
