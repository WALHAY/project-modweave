package git.walhay.modweave.api.category.dto

import git.walhay.modweave.api.category.Category
import git.walhay.modweave.api.category.CategoryId
import io.mcarle.konvert.api.KonvertTo

@KonvertTo(Category::class)
data class CategoryUpdateDto(val name: CategoryId, val description: String?)
