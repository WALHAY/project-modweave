package git.walhay.modweave.api.category.http.dto

import git.walhay.modweave.api.category.CategoryId
import git.walhay.modweave.api.category.command.CategoryUpdateCommand
import jakarta.validation.constraints.NotBlank

data class CategoryUpdateDto(
    @field:NotBlank val name: String,
    val description: String?,
) {
  fun toCategoryUpdateCommand(): CategoryUpdateCommand =
      CategoryUpdateCommand(CategoryId(name.trim()), description)
}
