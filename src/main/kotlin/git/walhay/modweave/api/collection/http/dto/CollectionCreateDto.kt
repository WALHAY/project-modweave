package git.walhay.modweave.api.collection.http.dto

import git.walhay.modweave.api.collection.command.CollectionCreateCommand
import io.mcarle.konvert.api.KonvertTo

@KonvertTo(CollectionCreateCommand::class)
data class CollectionCreateDto(val name: String, val description: String?)
