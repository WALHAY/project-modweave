package git.walhay.modweave.api.category.http.dto

import git.walhay.modweave.api.category.command.CategoryCreateCommand
import io.mcarle.konvert.api.KonvertTo

@KonvertTo(CategoryCreateCommand::class)
data class CategoryUploadDto(
    val name: String,
    val description: String?,
)
