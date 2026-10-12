package git.walhay.modweave.api.collection

import git.walhay.modweave.domain.collection.CollectionCreateCommand
import jakarta.validation.constraints.NotBlank

data class CollectionCreateDto(
    @field:NotBlank val name: String,
    val description: String?,
) {
  fun toCollectionCreateCommand(): CollectionCreateCommand =
      CollectionCreateCommand(name, description)
}
