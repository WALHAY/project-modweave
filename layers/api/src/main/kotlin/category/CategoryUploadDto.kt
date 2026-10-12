package git.walhay.modweave.api.category

import git.walhay.modweave.domain.category.CategoryCreateCommand
import git.walhay.modweave.domain.category.CategoryId
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CategoryUploadDto(
    @field:NotBlank @field:Size(max = 50) val name: String,
    val description: String? = null,
) {
  fun toCategoryCreateCommand(): CategoryCreateCommand =
      CategoryCreateCommand(CategoryId(name.trim()), description)
}
