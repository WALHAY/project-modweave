package git.walhay.modweave.domain.collection

class CollectionNotFoundException(
    collectionId: CollectionId,
) : RuntimeException("Collection with id=\"$collectionId\" not found")
