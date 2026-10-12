package git.walhay.modweave.domain.category

data class CategoryCreateCommand(
    val name: CategoryId,
    val description: String?,
)
