package git.walhay.modweave.api.category.http.dto

import git.walhay.modweave.api.category.CategoryId
import git.walhay.modweave.api.category.command.CategoryUpdateCommand

data class CategoryUpdateDto(
    val name: String,
    val description: String?,
) {
  fun toCategoryUpdateCommand(): CategoryUpdateCommand =
      CategoryUpdateCommand(CategoryId(name), description)
}
