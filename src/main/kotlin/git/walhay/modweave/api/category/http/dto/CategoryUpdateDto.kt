package git.walhay.modweave.api.category.http.dto

import git.walhay.modweave.api.category.command.CategoryUpdateCommand
import io.mcarle.konvert.api.KonvertTo

@KonvertTo(CategoryUpdateCommand::class)
data class CategoryUpdateDto(
    val name: String,
    val description: String?,
)
