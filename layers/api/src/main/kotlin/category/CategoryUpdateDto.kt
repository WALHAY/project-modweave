package git.walhay.modweave.api.category

import git.walhay.modweave.domain.category.CategoryId
import git.walhay.modweave.domain.category.CategoryUpdateCommand
import jakarta.validation.constraints.NotBlank

data class CategoryUpdateDto(
    @field:NotBlank val name: String,
    val description: String?,
) {
  fun toCategoryUpdateCommand(): CategoryUpdateCommand =
      CategoryUpdateCommand(CategoryId(name.trim()), description)
}
