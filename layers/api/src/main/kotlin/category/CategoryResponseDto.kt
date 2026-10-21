package git.walhay.modweave.api.category

import git.walhay.modweave.api.common.ApiResource
import git.walhay.modweave.domain.category.Category

data class CategoryResponseDto(
    val name: String,
    val description: String? = null,
) : ApiResource {
  companion object {
    fun fromCategory(category: Category): CategoryResponseDto =
        CategoryResponseDto(
            name = category.name.value,
            description = category.description,
        )
  }
}

data class CategoryCollectionDto<T>(val items: List<T>) : ApiResource
