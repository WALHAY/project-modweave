package git.walhay.modweave.domain.collection

data class CollectionCreateCommand(
    val name: String,
    val description: String?,
)
