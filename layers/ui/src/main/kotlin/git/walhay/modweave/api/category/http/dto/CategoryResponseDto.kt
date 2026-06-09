package git.walhay.modweave.api.category.http.dto

import git.walhay.modweave.api.category.Category
import git.walhay.modweave.api.category.CategoryId

data class CategoryResponseDto(
    val name: String,
    val description: String? = null,
) {
  companion object {
    fun fromCategory(category: Category): CategoryResponseDto =
        CategoryResponseDto(
            name = category.name.value,
            description = category.description,
        )
  }
}
