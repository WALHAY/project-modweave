package git.walhay.modweave.api.collection.http.dto

import git.walhay.modweave.api.collection.command.CollectionCreateCommand

data class CollectionCreateDto(
    val name: String,
    val description: String?,
) {
  fun toCollectionCreateCommand(): CollectionCreateCommand = CollectionCreateCommand(name, description)
}
