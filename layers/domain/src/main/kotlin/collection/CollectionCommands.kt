package git.walhay.modweave.domain.collection

data class CollectionCreateCommand(
    val name: String,
    val description: String?,
)

class CollectionNotFoundException(
    collectionId: CollectionId,
) : RuntimeException("Collection with id=\"$collectionId\" not found")
