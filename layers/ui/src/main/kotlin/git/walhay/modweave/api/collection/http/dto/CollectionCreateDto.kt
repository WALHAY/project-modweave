package git.walhay.modweave.api.collection.http.dto

import git.walhay.modweave.api.collection.command.CollectionCreateCommand
import jakarta.validation.constraints.NotBlank

data class CollectionCreateDto(
    @field:NotBlank val name: String,
    val description: String?,
) {
  fun toCollectionCreateCommand(): CollectionCreateCommand =
      CollectionCreateCommand(name, description)
}
