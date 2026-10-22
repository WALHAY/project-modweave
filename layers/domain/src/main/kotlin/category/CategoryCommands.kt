package git.walhay.modweave.domain.category

data class CategoryCommand(
    val name: CategoryId,
    val description: String?,
)

class CategoryExistsException(
    categoryId: CategoryId,
) : RuntimeException("Category with id=\"$categoryId\" already exists")

class CategoryNotFoundException(
    categoryId: CategoryId,
) : RuntimeException("Category with id=\"$categoryId\" not found")
