package git.walhay.modweave.api.category.http.dto

import git.walhay.modweave.api.category.CategoryId
import git.walhay.modweave.api.category.command.CategoryCreateCommand

data class CategoryUploadDto(
    val name: String,
    val description: String?,
) {
  fun toCategoryCreateCommand(): CategoryCreateCommand = CategoryCreateCommand(CategoryId(name), description)
}
