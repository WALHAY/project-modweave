package git.walhay.modweave.api.collection.exception

import git.walhay.modweave.api.collection.CollectionId

class CollectionNotFoundException(collectionId: CollectionId) :
    Exception("Collection with id=\"$collectionId\" not found")
