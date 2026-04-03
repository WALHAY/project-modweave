package git.walhay.modweave.api.collection.exception

import git.walhay.modweave.api.collection.CollectionId

class CollectionNotFoundException(val id: CollectionId) :
    Exception("Collection with id=\"${id.value}\" not found")
