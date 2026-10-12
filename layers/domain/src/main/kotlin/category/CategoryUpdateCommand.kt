package git.walhay.modweave.domain.category

data class CategoryUpdateCommand(
    val name: CategoryId,
    val description: String?,
)
