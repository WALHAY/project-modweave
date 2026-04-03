package git.walhay.modweave.api.category.dto

import git.walhay.modweave.api.category.Category
import git.walhay.modweave.api.category.CategoryId
import io.mcarle.konvert.api.KonvertTo
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

@KonvertTo(Category::class)
data class CategoryResponseDto(
    @field:NotEmpty @field:Size(min = 3) val name: CategoryId,
    val description: String? = null
)
