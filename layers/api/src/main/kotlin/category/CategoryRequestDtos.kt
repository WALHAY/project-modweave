package git.walhay.modweave.api.category

import com.fasterxml.jackson.annotation.JsonProperty
import git.walhay.modweave.domain.category.CategoryCommand
import git.walhay.modweave.domain.category.CategoryId
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CategoryUploadDto(
    @field:NotBlank @field:Size(max = 50) val name: String,
    val description: String? = null,
) {
  fun toCategoryCommand(): CategoryCommand = CategoryCommand(CategoryId(name.trim()), description)
}

data class CategoryPatchDto(@param:JsonProperty(required = true) val description: String?)

data class CategoryUpdateDto(
    @field:NotBlank val name: String,
    val description: String?,
) {
  fun toCategoryCommand(): CategoryCommand = CategoryCommand(CategoryId(name.trim()), description)
}
