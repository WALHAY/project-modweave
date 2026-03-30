package git.walhay.modweave.api.category

import git.walhay.modweave.api.category.dto.CategoryDto
import git.walhay.modweave.api.category.repository.CategoryEntity
import io.mcarle.konvert.api.KonvertTo

@KonvertTo(CategoryEntity::class, mapFunctionName = "toEntity")
@KonvertTo(CategoryDto::class)
data class Category(
    var name: String,
    var description: String? = null,
)
