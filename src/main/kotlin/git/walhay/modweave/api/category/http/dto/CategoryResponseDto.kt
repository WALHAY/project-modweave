package git.walhay.modweave.api.category.http.dto

import git.walhay.modweave.api.category.Category
import git.walhay.modweave.api.category.CategoryId
import io.mcarle.konvert.api.KonvertFrom

@KonvertFrom(Category::class)
data class CategoryResponseDto(
    val name: CategoryId,
    val description: String? = null,
) {
  companion object
}
