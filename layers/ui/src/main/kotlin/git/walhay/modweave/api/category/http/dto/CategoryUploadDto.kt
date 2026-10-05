package git.walhay.modweave.api.category.http.dto

import git.walhay.modweave.api.category.CategoryId
import git.walhay.modweave.api.category.command.CategoryCreateCommand
import jakarta.validation.constraints.NotBlank

data class CategoryUploadDto(
    @field:NotBlank val name: String,
    val description: String?,
) {
  fun toCategoryCreateCommand(): CategoryCreateCommand =
      CategoryCreateCommand(CategoryId(name.trim()), description)
}
