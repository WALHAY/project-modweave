package git.walhay.modweave.api.category

import git.walhay.modweave.api.category.dto.CategoryResponseDto
import git.walhay.modweave.api.category.repository.CategoryEntity
import io.mcarle.konvert.api.KonvertTo

@KonvertTo(CategoryEntity::class, mapFunctionName = "toEntity")
@KonvertTo(CategoryResponseDto::class)
data class Category(
    var name: CategoryId,
    var description: String? = null,
)
